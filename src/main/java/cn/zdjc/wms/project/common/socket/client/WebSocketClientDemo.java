package cn.zdjc.wms.project.common.socket.client;

import cn.zdjc.wms.project.common.socket.TerminalWebSocketClient;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.*;

@Slf4j
public class WebSocketClientDemo {

    // 心跳间隔（秒）- 建议与服务端心跳超时时间匹配（如服务端60秒超时，则设为30秒）
    private static final int HEARTBEAT_INTERVAL = 30;
    // 等待连接建立最大重试次数
    private static final int MAX_CONNECT_WAIT = 10;

    public static void main(String[] args) {
        String serverUrl = "ws://localhost:4660/app";
        String terminalId = "workstationCode03";
        ScheduledExecutorService heartbeatScheduler = null;

        log.info("🚀 启动WebSocket客户端 - 服务端: {}, 终端ID: {}", serverUrl, terminalId);

        try {
            TerminalWebSocketClient client = new TerminalWebSocketClient(serverUrl, terminalId);
            log.info("🔌 正在连接到服务端...");
            client.connect();

            // 等待连接建立
            int waitCount = 0;
            while (!client.isConnected() && waitCount < MAX_CONNECT_WAIT) {
                Thread.sleep(1000);
                waitCount++;
                log.debug("⏳ 等待连接... ({}秒)", waitCount);
            }

            if (!client.isConnected()) {
                log.error("❌ 连接超时！请检查服务端是否启动并监听8080端口");
                return;
            }

            log.info("✅ 客户端连接成功！");

            // =============== 关键修改：启动周期性心跳 ===============
            heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
            ScheduledExecutorService finalHeartbeatScheduler = heartbeatScheduler;
            heartbeatScheduler.scheduleAtFixedRate(() -> {
                try {
                    if (client.isConnected()) {
                        client.sendHeartbeat();
                        log.debug("💓 心跳已发送");
                    } else {
                        log.warn("⚠️ 连接已断开，停止发送心跳");
                        finalHeartbeatScheduler.shutdown();
                    }
                } catch (Exception e) {
                    log.error("❌ 心跳发送异常", e);
                    // 不主动关闭连接，交由主循环检测状态
                }
            }, 0, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
            log.info("🔄 已启动周期性心跳（间隔 {} 秒）", HEARTBEAT_INTERVAL);
            // =======================================================

            // 执行其他业务操作（状态更新、任务等）
            client.sendStatusUpdate("RUNNING");
            Thread.sleep(2000);
            client.sendTaskComplete("TASK_001", "SUCCESS");
            Thread.sleep(2000);
            client.sendAlarm("LOW_BATTERY", "电量低于20%");

            // =============== 主循环：持续监听连接状态 ===============
            log.info("⏳ 客户端保持运行（连接断开时自动退出）...");
            while (client.isConnected()) {
                Thread.sleep(1000); // 低开销轮询
            }
            log.info("🔌 检测到连接断开（服务端关闭或网络异常）");
            // =======================================================

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("⚠️ 客户端被中断");
        } catch (Exception e) {
            log.error("❌ 客户端运行异常", e);
        } finally {
            // 清理资源
            if (heartbeatScheduler != null && !heartbeatScheduler.isShutdown()) {
                heartbeatScheduler.shutdownNow();
                log.info("✅ 心跳调度器已关闭");
            }
            // TerminalWebSocketClient应在onClose回调中自动清理连接，此处避免重复关闭
            log.info("👋 客户端已安全退出");
            System.exit(0);
        }
    }
}