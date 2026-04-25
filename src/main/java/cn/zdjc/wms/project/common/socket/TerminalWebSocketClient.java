package cn.zdjc.wms.project.common.socket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WebSocket客户端（Java-WebSocket库）
 * 与Spring @ServerEndpoint服务端兼容
 */
@Slf4j
public class TerminalWebSocketClient extends WebSocketClient {

    private final String terminalId;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);
    private volatile boolean shouldReconnect = true;
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final int RECONNECT_BASE_DELAY = 3; // 秒

    public TerminalWebSocketClient(String serverUrl, String terminalId) throws Exception {
        super(new URI(serverUrl + "/websocket/" + terminalId));
        this.terminalId = terminalId;
        this.setConnectionLostTimeout(30);
        log.info("🔧 创建WebSocket客户端 - 终端ID: {}, 服务端地址: {}", terminalId, getURI());
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        log.info("✅ WebSocket连接成功 - 终端ID: {}", terminalId);
        isConnected.set(true);
        reconnectAttempts = 0;
        // 连接成功后发送心跳
        startHeartbeat();
    }

    @Override
    public void onMessage(String message) {
        try {
            log.info("📨 收到服务端消息 - 终端ID: {}, 消息: {}", terminalId, message);
            JSONObject jsonMsg = JSON.parseObject(message);
            String msgType = jsonMsg.getString("type");

            // 处理不同类型的消息
            switch (msgType) {

                case "heartbeat":
                    log.debug("🏓 收到心跳响应 - 终端ID: {}", terminalId);
                    break;
                case "notify":
                    handleNotify(jsonMsg);
                    break;
                case "success":
                    handleSuccess(jsonMsg);
                    break;
                case "error":
                    handleError(jsonMsg);
                    break;
                default:
                    log.warn("⚠️ 未知消息类型: {}", msgType);
            }
        } catch (Exception e) {
            log.error("❌ 处理消息异常 - 终端ID: {}", terminalId, e);
        }
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        log.info("📨 收到二进制消息 - 长度: {}", bytes.remaining());
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        isConnected.set(false);
        log.info("👋 WebSocket连接关闭 - 终端ID: {}, Code: {}, Reason: {}, Remote: {}",
                terminalId, code, reason, remote);

        // 尝试重连
        if (shouldReconnect && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            reconnectAttempts++;
            int delay = RECONNECT_BASE_DELAY * reconnectAttempts;
            log.info("🔄 尝试重连 ({}/{})... 延迟: {}秒", reconnectAttempts, MAX_RECONNECT_ATTEMPTS, delay);
            scheduler.schedule(this::reconnect, delay, TimeUnit.SECONDS);
        } else if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            log.error("❌ 重连次数已达上限，停止重连 - 终端ID: {}", terminalId);
        }
    }

    @Override
    public void onError(Exception ex) {
        log.error("❌ WebSocket错误 - 终端ID: {}", terminalId, ex);
    }

    /**
     * 处理通知消息
     */
    private void handleNotify(JSONObject message) {
        String notifyType = message.getString("message");
        JSONObject data = message.getJSONObject("data");
        log.info("🔔 收到通知 - 类型: {}, 数据: {}", notifyType, data);
        // 根据通知类型执行相应操作
    }

    /**
     * 处理成功响应
     */
    private void handleSuccess(JSONObject message) {
        String msg = message.getString("message");
        log.info("✅ 操作成功: {}", msg);
    }

    /**
     * 处理错误响应
     */
    private void handleError(JSONObject message) {
        String errorMsg = message.getString("message");
        log.error("❌ 操作失败: {}", errorMsg);
    }

    /**
     * 启动心跳
     */
    private void startHeartbeat() {
        // 立即发送一次心跳
        sendHeartbeat();

        // 每30秒发送一次心跳
        scheduler.scheduleAtFixedRate(() -> {
            if (isConnected.get() && isOpen()) {
                sendHeartbeat();
            }
        }, 30, 30, TimeUnit.SECONDS);

        log.info("💓 心跳机制已启动 - 终端ID: {}", terminalId);
    }

    /**
     * 发送心跳
     */
    public void sendHeartbeat() {
        JSONObject heartbeat = new JSONObject();
        heartbeat.put("type", "heartbeat");
        heartbeat.put("data", new JSONObject() {{
            put("timestamp", System.currentTimeMillis());
        }});
        send(heartbeat.toJSONString());
        log.debug("💓 发送心跳 - 终端ID: {}", terminalId);
    }

    /**
     * 发送状态更新
     */
    public void sendStatusUpdate(String status) {
        JSONObject message = new JSONObject();
        message.put("type", "status_update");
        message.put("data", new JSONObject() {{
            put("status", status);
            put("timestamp", System.currentTimeMillis());
        }});
        send(message.toJSONString());
        log.info("📊 发送状态更新 - 终端ID: {}, 状态: {}", terminalId, status);
    }

    /**
     * 发送任务完成消息
     */
    public void sendTaskComplete(String taskId, String result) {
        JSONObject message = new JSONObject();
        message.put("type", "task_complete");
        message.put("data", new JSONObject() {{
            put("taskId", taskId);
            put("result", result);
            put("timestamp", System.currentTimeMillis());
        }});
        send(message.toJSONString());
        log.info("✅ 发送任务完成 - 终端ID: {}, 任务ID: {}, 结果: {}", terminalId, taskId, result);
    }

    /**
     * 发送告警消息
     */
    public void sendAlarm(String alarmType, String message) {
        JSONObject alarmMsg = new JSONObject();
        alarmMsg.put("type", "alarm");
        alarmMsg.put("data", new JSONObject() {{
            put("alarmType", alarmType);
            put("message", message);
            put("timestamp", System.currentTimeMillis());
        }});
        send(alarmMsg.toJSONString());
        log.warn("🚨 发送告警 - 终端ID: {}, 告警类型: {}", terminalId, alarmType);
    }

    /**
     * 关闭连接
     */
    public void shutdown() {
        log.info("🛑 关闭WebSocket连接 - 终端ID: {}", terminalId);
        shouldReconnect = false;
        close();
        scheduler.shutdown();
    }

    /**
     * 检查连接状态
     */
    public boolean isConnected() {
        return isConnected.get() && isOpen();
    }
}