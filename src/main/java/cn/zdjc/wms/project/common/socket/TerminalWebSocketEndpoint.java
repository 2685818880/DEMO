package cn.zdjc.wms.project.common.socket;

import cn.zdjc.wms.project.service.workstation.WorkstationService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 终端WebSocket服务端
 *
 * <p>优化点：</p>
 * <ul>
 *   <li>✅ 使用迭代器安全遍历 SESSIONS，避免并发修改异常</li>
 *   <li>✅ 添加心跳超时检测定时任务</li>
 *   <li>✅ 优化异常处理和日志输出</li>
 *   <li>✅ 提供完整的终端监控接口</li>
 *   <li>✅ 优化广播方法的并发安全性</li>
 *   <li>✅ 添加终端统计信息</li>
 * </ul>
 */
@Component
@Slf4j
@ServerEndpoint(
        value = "/websocket/{Id}",
        configurator = SpringWebSocketConfigurator.class
)
public class TerminalWebSocketEndpoint {

    public TerminalWebSocketEndpoint() {
        log.info("🔧 TerminalWebSocketEndpoint Bean 已创建");
    }

    // ==================== 静态字段 ====================

    /**
     * 存储所有连接的终端会话（public static，供监控服务访问）
     */
    public static final Map<String, TerminalSession> SESSIONS = new ConcurrentHashMap<>();

    /**
     * 在线终端数量
     */
    public static final AtomicInteger ONLINE_COUNT = new AtomicInteger(0);

    /**
     * 总连接次数（历史累计）
     */
    private static final AtomicInteger TOTAL_CONNECTIONS = new AtomicInteger(0);

    /**
     * 今日连接次数（可按天重置）
     */
    private static final AtomicInteger TODAY_CONNECTIONS = new AtomicInteger(0);

    // ==================== 实例字段 ====================

    private Session session;
    private String terminalId;

    @Autowired
    private WorkstationService workstationService;

    // ==================== WebSocket 生命周期方法 ====================

    @OnOpen
    public void onOpen(Session session, @PathParam("Id") String terminalId) {
        this.session = session;
        this.terminalId = terminalId;

        try {
            // 增加计数
            int currentOnline = ONLINE_COUNT.incrementAndGet();
            TOTAL_CONNECTIONS.incrementAndGet();
            TODAY_CONNECTIONS.incrementAndGet();

            // 创建会话
            TerminalSession terminalSession = new TerminalSession(session, terminalId);
            SESSIONS.put(terminalId, terminalSession);

            // 设置超时（120秒空闲超时）
            session.setMaxIdleTimeout(120_000);

            log.info("✅ 终端连接建立成功 - 终端ID: {}, 客户端: {}, 在线数: {}, 总连接: {}",
                    terminalId, getClientAddress(session), currentOnline, TOTAL_CONNECTIONS.get());

            // 通知业务服务
            notifyBusinessOnOpen(terminalId);

            // 回复客户端
            sendMessage(ResponseMessage.success("连接成功", terminalId));

        } catch (Exception e) {
            log.error("❌ 终端连接建立失败 - 终端ID: {}", terminalId, e);
            cleanupOnFailure(terminalId, session, "连接初始化失败");
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        String closeTerminalId = this.terminalId;

        if (closeTerminalId == null) {
            log.warn("⚠️ terminalId 为空，无法清理会话");
            return;
        }

        try {
            // 从 SESSIONS 中移除
            TerminalSession removed = SESSIONS.remove(closeTerminalId);

            if (removed != null) {
                int currentOnline = ONLINE_COUNT.decrementAndGet();
                log.info("👋 终端连接关闭 - 终端ID: {}, 原因: [{}], 在线数: {}",
                        closeTerminalId, getCloseReasonDesc(closeReason), currentOnline);

                // 通知业务服务
                notifyBusinessOnClose(closeTerminalId);
            } else {
                log.warn("⚠️ 会话已提前清理 - 终端ID: {}", closeTerminalId);
            }

        } catch (Exception e) {
            log.error("❌ onClose 处理异常 - 终端ID: {}", closeTerminalId, e);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        if (isBlank(message)) {
            log.warn("⚠️ 收到空消息 - 终端ID: {}", terminalId);
            sendMessage(ResponseMessage.error("消息不能为空"));
            return;
        }

        try {
            log.debug("📨 收到终端消息 - 终端ID: {}, 消息: {}", terminalId, message);

            String trimmed = message.trim();

            // 心跳检测 - 简单字符串
            if (handleSimpleHeartbeat(trimmed)) {
                return;
            }

            // JSON 消息处理
            JSONObject jsonMsg = parseMessage(message);
            if (jsonMsg == null) {
                return;
            }

            // 检查必需字段
            if (!jsonMsg.containsKey("type")) {
                log.warn("⚠️ 消息缺少 type 字段 - 终端ID: {}", terminalId);
                sendMessage(ResponseMessage.error("消息缺少 type 字段"));
                return;
            }

            // 处理业务消息
            String msgType = jsonMsg.getString("type");
            JSONObject data = jsonMsg.getJSONObject("data");
            handleMessageByType(msgType, data);

        } catch (Exception e) {
            log.error("❌ 处理终端消息异常 - 终端ID: {}", terminalId, e);
            sendMessage(ResponseMessage.error("消息处理失败: " + e.getMessage()));
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        String errorTerminalId = this.terminalId;
        String errorType = getErrorType(error);

        log.error("❌ 终端连接异常 [{}] - 终端ID: {}", errorType, errorTerminalId, error);

        // WebSocket 容器会保证调用 onClose 进行清理，此处不重复清理
    }

    // ==================== 消息处理方法 ====================

    /**
     * 处理简单心跳（ping/pong）
     *
     * @param message 消息内容
     * @return 是否已处理
     */
    private boolean handleSimpleHeartbeat(String message) {
        if ("ping".equalsIgnoreCase(message)) {
            handleHeartbeat(null);
            log.debug("🏓 回复心跳（字符串 ping） - 终端ID: {}", terminalId);
            return true;
        }

        if ("pong".equalsIgnoreCase(message)) {
            TerminalSession ts = SESSIONS.get(terminalId);
            if (ts != null) {
                ts.setLastHeartbeatTime(System.currentTimeMillis());
                log.debug("💓 收到客户端 pong - 终端ID: {}", terminalId);
            }
            return true;
        }

        return false;
    }

    /**
     * 处理心跳（更新时间）
     */
    private void handleHeartbeat(JSONObject data) {
        TerminalSession terminalSession = SESSIONS.get(terminalId);
        if (terminalSession != null) {
            terminalSession.setLastHeartbeatTime(System.currentTimeMillis());
            log.debug("💓 终端心跳 - 终端ID: {}", terminalId);
        }
        sendMessage(ResponseMessage.pong());
    }

    /**
     * 处理状态更新
     */
    private void handleStatusUpdate(JSONObject data) {
        if (data == null) {
            log.warn("⚠️ status_update 消息数据为空 - 终端ID: {}", terminalId);
            sendMessage(ResponseMessage.error("状态数据不能为空"));
            return;
        }

        String status = data.getString("status");
        if (isBlank(status)) {
            log.warn("⚠️ status_update 缺少 status 字段 - 终端ID: {}", terminalId);
            sendMessage(ResponseMessage.error("缺少 status 字段"));
            return;
        }

        log.info("📊 终端状态更新 - 终端ID: {}, 状态: {}", terminalId, status);
        sendMessage(ResponseMessage.success("状态更新成功", status));
    }

    /**
     * 处理任务完成
     */
    private void handleTaskComplete(JSONObject data) {
        if (data == null) {
            log.warn("⚠️ task_complete 消息数据为空 - 终端ID: {}", terminalId);
            sendMessage(ResponseMessage.error("任务数据不能为空"));
            return;
        }

        String taskId = data.getString("taskId");
        String result = data.getString("result");

        log.info("✅ 终端任务完成 - 终端ID: {}, 任务ID: {}, 结果: {}", terminalId, taskId, result);
        sendMessage(ResponseMessage.success("任务完成确认", taskId));
    }

    /**
     * 处理告警
     */
    private void handleAlarm(JSONObject data) {
        if (data == null) {
            log.warn("⚠️ alarm 消息数据为空 - 终端ID: {}", terminalId);
            sendMessage(ResponseMessage.error("告警数据不能为空"));
            return;
        }

        String alarmType = data.getString("alarmType");
        String alarmMsg = data.getString("message");

        log.warn("🚨 终端告警 - 终端ID: {}, 类型: {}, 信息: {}", terminalId, alarmType, alarmMsg);
        sendMessage(ResponseMessage.success("告警已接收", alarmType));
    }

    /**
     * 根据消息类型分发处理
     */
    private void handleMessageByType(String msgType, JSONObject data) {
        switch (msgType) {
            case "heartbeat":
                handleHeartbeat(data);
                break;
            case "status_update":
                handleStatusUpdate(data);
                break;
            case "task_complete":
                handleTaskComplete(data);
                break;
            case "alarm":
                handleAlarm(data);
                break;
            default:
                log.warn("⚠️ 未知消息类型: {}", msgType);
                sendMessage(ResponseMessage.error("不支持的消息类型: " + msgType));
        }
    }

    /**
     * 解析消息为 JSON
     */
    private JSONObject parseMessage(String message) {
        try {
            return JSON.parseObject(message);
        } catch (Exception e) {
            log.warn("⚠️ 消息格式错误（非JSON） - 终端ID: {}, 消息: {}", terminalId, message);
            sendMessage(ResponseMessage.error("消息格式错误，请发送JSON格式"));
            return null;
        }
    }

    // ==================== 业务通知方法 ====================

    /**
     * 通知业务层终端连接
     */
    private void notifyBusinessOnOpen(String terminalId) {
        Optional.ofNullable(workstationService)
                .ifPresentOrElse(
                        service -> {
                            try {
                                service.wsOpenStation(terminalId);
                            } catch (Exception e) {
                                log.error("通知业务层连接失败 - 终端ID: {}", terminalId, e);
                            }
                        },
                        () -> log.warn("⚠️ workstationService 未注入，跳过业务通知 - 终端ID: {}", terminalId)
                );
    }

    /**
     * 通知业务层终端断开
     */
    private void notifyBusinessOnClose(String terminalId) {
        Optional.ofNullable(workstationService)
                .ifPresentOrElse(
                        service -> {
                            try {
                                service.wsCloseStation(terminalId);
                            } catch (Exception e) {
                                log.error("通知业务层断开失败 - 终端ID: {}", terminalId, e);
                            }
                        },
                        () -> log.debug("workstationService 未注入，跳过断开通知 - 终端ID: {}", terminalId)
                );
    }

    // ==================== 消息发送方法 ====================

    /**
     * 发送消息给当前终端
     */
    private void sendMessage(Object message) {
        try {
            if (session != null && session.isOpen()) {
                String jsonMessage = JSON.toJSONString(message);
                session.getBasicRemote().sendText(jsonMessage);
                log.debug("📤 回复消息 - 终端ID: {}", terminalId);
            } else {
                log.warn("⚠️ 会话已关闭，无法发送消息 - 终端ID: {}", terminalId);
            }
        } catch (IOException e) {
            log.error("❌ 回复消息失败 - 终端ID: {}", terminalId, e);
        }
    }

    /**
     * 发送消息给指定终端（静态方法，供外部调用）
     */
    public static boolean sendToTerminal(String terminalId, Object message) {
        if (terminalId == null || message == null) {
            log.warn("⚠️ sendToTerminal 参数为空 - terminalId: {}", terminalId);
            return false;
        }

        TerminalSession terminalSession = SESSIONS.get(terminalId);
        if (terminalSession != null && terminalSession.getSession().isOpen()) {
            try {
                String jsonMessage = JSON.toJSONString(message);
                terminalSession.getSession().getBasicRemote().sendText(jsonMessage);
                log.info("📤 消息发送到终端 - 终端ID: {}", terminalId);
                return true;
            } catch (IOException e) {
                log.error("❌ 发送消息失败 - 终端ID: {}", terminalId, e);
                // 不在此处清理，由 onClose 处理
            }
        } else {
            log.warn("⚠️ 终端不在线或会话已关闭 - 终端ID: {}", terminalId);
        }
        return false;
    }

    /**
     * 广播消息给所有在线终端（使用迭代器避免并发修改异常）
     */
    public static void broadcast(Object message) {
        if (message == null) {
            log.warn("⚠️ 广播消息为空");
            return;
        }

        String jsonMessage = JSON.toJSONString(message);
        int totalCount = SESSIONS.size();
        log.info("📢 广播消息 - 类型: {}, 在线终端数: {}",
                getMessageType(message), totalCount);

        if (totalCount == 0) {
            log.warn("⚠️ 无在线终端，跳过广播");
            return;
        }

        List<String> failedTerminals = new ArrayList<>();
        Iterator<Map.Entry<String, TerminalSession>> iterator = SESSIONS.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, TerminalSession> entry = iterator.next();
            String tid = entry.getKey();
            TerminalSession terminalSession = entry.getValue();

            try {
                Session sess = terminalSession.getSession();
                if (sess != null && sess.isOpen()) {
                    sess.getBasicRemote().sendText(jsonMessage);
                } else {
                    log.warn("⚠️ 会话已关闭 - 终端ID: {}", tid);
                    failedTerminals.add(tid);
                    iterator.remove();
                    ONLINE_COUNT.decrementAndGet();
                }
            } catch (IOException e) {
                log.error("❌ 广播失败 - 终端ID: {}", tid, e);
                failedTerminals.add(tid);
                iterator.remove();
                ONLINE_COUNT.decrementAndGet();
            }
        }

        if (!failedTerminals.isEmpty()) {
            log.info("🧹 清理失效会话 - 数量: {}", failedTerminals.size());
        }
    }

    /**
     * 获取消息类型
     */
    private static String getMessageType(Object message) {
        if (message instanceof ResponseMessage) {
            return ((ResponseMessage) message).getType();
        }
        return message.getClass().getSimpleName();
    }

    // ==================== 工具方法 ====================

    /**
     * 获取客户端地址
     */
    private String getClientAddress(Session session) {
        try {
            Object ipObj = session.getUserProperties().get("client-ip");
            if (ipObj != null) {
                return ipObj.toString();
            }
            return session.getId().substring(0, Math.min(8, session.getId().length())) + "...";
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 关闭原因描述
     */
    private String getCloseReasonDesc(CloseReason closeReason) {
        if (closeReason == null) {
            return "未知";
        }
        return String.format("%s (%d)",
                closeReason.getReasonPhrase(),
                closeReason.getCloseCode().getCode());
    }

    /**
     * 错误类型
     */
    private String getErrorType(Throwable error) {
        if (error instanceof IOException) {
            return "IO异常";
        } else if (error instanceof EncodeException || error instanceof DecodeException) {
            return "编解码异常";
        }
        return error.getClass().getSimpleName();
    }

    /**
     * 判断字符串是否为空或空白
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 连接失败时的清理操作
     */
    private void cleanupOnFailure(String terminalId, Session session, String reason) {
        if (terminalId != null) {
            SESSIONS.remove(terminalId);
            ONLINE_COUNT.decrementAndGet();
        }

        try {
            if (session != null && session.isOpen()) {
                session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, reason));
            }
        } catch (IOException e) {
            log.error("❌ 清理失败会话时出错", e);
        }
    }

    // ==================== 监控统计方法 ====================

    /**
     * 获取在线终端数量
     */
    public static int getOnlineCount() {
        return ONLINE_COUNT.get();
    }

    /**
     * 获取总连接次数（历史累计）
     */
    public static int getTotalConnections() {
        return TOTAL_CONNECTIONS.get();
    }

    /**
     * 获取今日连接次数
     */
    public static int getTodayConnections() {
        return TODAY_CONNECTIONS.get();
    }

    /**
     * 获取所有在线终端ID
     */
    public static List<String> getOnlineTerminalIds() {
        return new ArrayList<>(SESSIONS.keySet());
    }

    /**
     * 检查终端是否在线
     */
    public static boolean isOnline(String terminalId) {
        TerminalSession session = SESSIONS.get(terminalId);
        return session != null && session.getSession().isOpen();
    }

    /**
     * 获取所有终端信息
     */
    public static List<TerminalInfo> getAllTerminalInfo() {
        List<TerminalInfo> infos = new ArrayList<>();

        SESSIONS.forEach((terminalId, session) -> {
            TerminalInfo info = buildTerminalInfo(terminalId, session);
            if (info != null) {
                infos.add(info);
            }
        });

        return infos;
    }

    /**
     * 获取单个终端信息
     */
    public static TerminalInfo getTerminalInfo(String terminalId) {
        TerminalSession session = SESSIONS.get(terminalId);
        if (session == null) {
            return null;
        }
        return buildTerminalInfo(terminalId, session);
    }

    /**
     * 构建终端信息
     */
    private static TerminalInfo buildTerminalInfo(String terminalId, TerminalSession session) {
        try {
            TerminalInfo info = new TerminalInfo();
            info.setTerminalId(terminalId);

            Session sess = session.getSession();
            if (sess == null) {
                return null;
            }

            Map<String, Object> props = sess.getUserProperties();
            info.setClientIp(String.valueOf(props.getOrDefault("client-ip", "unknown")));
            info.setUserAgent(String.valueOf(props.getOrDefault("user-agent", "unknown")));

            info.setConnectTime(session.getConnectTime());
            info.setLastHeartbeatTime(session.getLastHeartbeatTime());
            info.setOnlineDuration(session.getOnlineDuration());
            info.setHeartbeatElapsed(session.getHeartbeatElapsed());
            info.setIsActive(sess.isOpen());

            return info;
        } catch (Exception e) {
            log.error("构建终端信息失败 - 终端ID: {}", terminalId, e);
            return null;
        }
    }

    /**
     * 获取终端统计信息
     */
    public static TerminalStatistics getStatistics() {
        TerminalStatistics stats = new TerminalStatistics();
        stats.setOnlineCount(ONLINE_COUNT.get());
        stats.setTotalConnections(TOTAL_CONNECTIONS.get());
        stats.setTodayConnections(TODAY_CONNECTIONS.get());

        // 计算平均在线时长
        long totalDuration = 0;
        int activeCount = 0;

        for (TerminalSession session : SESSIONS.values()) {
            try {
                if (session.getSession().isOpen()) {
                    totalDuration += session.getOnlineDuration();
                    activeCount++;
                }
            } catch (Exception e) {
                // 忽略异常
            }
        }

        stats.setAverageOnlineDuration(activeCount > 0 ? totalDuration / activeCount : 0);
        return stats;
    }

    // ==================== 心跳超时检测定时任务 ====================

    /**
     * 心跳超时检测（每30秒执行一次）
     * 超时阈值：90秒
     */
    @Component
    @Slf4j
    public static class HeartbeatMonitor {

        private static final long HEARTBEAT_TIMEOUT = 90_000; // 90秒

        @Scheduled(fixedRate = 30_000) // 每30秒检查一次
        public void checkHeartbeatTimeout() {
            if (SESSIONS.isEmpty()) {
                return;
            }

            long currentTime = System.currentTimeMillis();
            List<String> timeoutTerminals = new ArrayList<>();

            SESSIONS.forEach((terminalId, session) -> {
                try {
                    long elapsed = currentTime - session.getLastHeartbeatTime();
                    if (elapsed > HEARTBEAT_TIMEOUT && session.getSession().isOpen()) {
                        timeoutTerminals.add(terminalId);
                        log.warn("⏰ 终端心跳超时 - 终端ID: {}, 超时时间: {}秒",
                                terminalId, elapsed / 1000);
                    }
                } catch (Exception e) {
                    log.error("检查心跳超时时出错 - 终端ID: {}", terminalId, e);
                }
            });

            // 关闭超时连接
            timeoutTerminals.forEach(terminalId -> {
                TerminalSession session = SESSIONS.get(terminalId);
                if (session != null) {
                    try {
                        session.getSession().close(new CloseReason(
                                CloseReason.CloseCodes.GOING_AWAY,
                                "心跳超时"));
                        log.info("🔌 已关闭超时连接 - 终端ID: {}", terminalId);
                    } catch (IOException e) {
                        log.error("关闭超时连接失败 - 终端ID: {}", terminalId, e);
                    }
                }
            });
        }
    }

    // ==================== 内部类 ====================

    /**
     * 终端会话包装类
     */
    @Data
    public static class TerminalSession {
        private Session session;
        private String terminalId;
        private long connectTime;
        private long lastHeartbeatTime;

        public TerminalSession(Session session, String terminalId) {
            this.session = session;
            this.terminalId = terminalId;
            this.connectTime = System.currentTimeMillis();
            this.lastHeartbeatTime = System.currentTimeMillis();
        }

        /**
         * 获取在线时长（秒）
         */
        public long getOnlineDuration() {
            return (System.currentTimeMillis() - connectTime) / 1000;
        }

        /**
         * 获取心跳间隔（秒）
         */
        public long getHeartbeatElapsed() {
            return (System.currentTimeMillis() - lastHeartbeatTime) / 1000;
        }
    }

    /**
     * 终端信息
     */
    @Data
    public static class TerminalInfo {
        private String terminalId;
        private String clientIp;
        private String userAgent;
        private long connectTime;
        private long lastHeartbeatTime;
        private long onlineDuration;      // 秒
        private long heartbeatElapsed;    // 秒
        private boolean isActive;


        public void setIsActive(boolean isActive) {
            this.isActive=isActive;
        }
    }

    /**
     * 终端统计信息
     */
    @Data
    public static class TerminalStatistics {
        private int onlineCount;           // 当前在线数
        private int totalConnections;      // 总连接次数
        private int todayConnections;      // 今日连接次数
        private long averageOnlineDuration; // 平均在线时长（秒）
    }
}