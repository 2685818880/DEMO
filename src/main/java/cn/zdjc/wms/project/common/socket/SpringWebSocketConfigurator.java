package cn.zdjc.wms.project.common.socket;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring WebSocket配置器 - 解决@ServerEndpoint中无法注入Bean的问题
 *
 * <p>修复重点：</p>
 * <ul>
 *   <li>✅ 移除不存在的 getRemoteAddress() 调用</li>
 *   <li>✅ 仅依赖标准 HTTP Header 获取客户端 IP</li>
 *   <li>✅ 统一使用小写 Header key</li>
 *   <li>✅ 增强调试日志</li>
 *   <li>✅ 优化线程安全</li>
 * </ul>
 */
@Component
@Slf4j
public class SpringWebSocketConfigurator extends ServerEndpointConfig.Configurator
        implements ApplicationContextAware {

    /**
     * Spring 应用上下文（静态，供 WebSocket 端点使用）
     */
    @Getter
    private static volatile ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        if (applicationContext == null) {
            synchronized (SpringWebSocketConfigurator.class) {
                if (applicationContext == null) {
                    applicationContext = context;
                    log.info("✅ SpringWebSocketConfigurator 初始化完成");
                }
            }
        }
    }

    public static <T> T getBean(Class<T> clazz) {
        if (applicationContext == null) {
            throw new IllegalStateException(
                    "ApplicationContext 未初始化，无法获取 Bean: " + clazz.getName());
        }
        return applicationContext.getBean(clazz);
    }

    public static <T> boolean containsBean(Class<T> clazz) {
        return applicationContext != null && applicationContext.containsBean(clazz.getName());
    }

    @Override
    public <T> T getEndpointInstance(Class<T> clazz) throws InstantiationException {
        if (applicationContext == null) {
            log.error("ApplicationContext 未初始化，无法创建端点实例: {}", clazz.getName());
            throw new InstantiationException("ApplicationContext 未初始化: " + clazz.getName());
        }
        try {
            return applicationContext.getBean(clazz);
        } catch (BeansException e) {
            log.error("创建 WebSocket 端点实例失败 - 类型: {}", clazz.getName(), e);
            throw new InstantiationException("创建端点失败: " + e.getMessage());
        }
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig config,
                                HandshakeRequest request,
                                HandshakeResponse response) {
        try {
            // 1. 提取并保存所有请求头（统一小写 key）
            Map<String, String> headers = extractHeaders(request);
            config.getUserProperties().putAll(headers);

            // 2. 获取客户端 IP（仅依赖标准 Header）
            String clientIp = getClientIpFromHandshake(request);
            config.getUserProperties().put("client-ip", clientIp);

            // 3. 保存 User-Agent
            String userAgent = headers.getOrDefault("user-agent", "unknown");
            config.getUserProperties().put("user-agent", userAgent);

            // 4. 保存握手时间
            config.getUserProperties().put("handshake-time", System.currentTimeMillis());

            // 5. 保存原始请求（供高级使用）
            config.getUserProperties().put("handshake-request", request);

            log.debug("🔌 WebSocket 握手 - 客户端IP: {}, User-Agent: {}",
                    clientIp,
                    userAgent.length() > 60 ? userAgent.substring(0, 60) + "..." : userAgent);

        } catch (Exception e) {
            log.error("modifyHandshake 异常", e);
        }

        super.modifyHandshake(config, request, response);
    }

    /**
     * 提取所有请求头（key 统一小写）
     */
    private Map<String, String> extractHeaders(HandshakeRequest request) {
        Map<String, String> headers = new HashMap<>();
        Map<String, List<String>> rawHeaders = request.getHeaders();

        if (rawHeaders != null) {
            rawHeaders.forEach((key, values) -> {
                if (values != null && !values.isEmpty()) {
                    headers.put(key.toLowerCase(), values.get(0));
                }
            });
        }
        return headers;
    }

    /**
     * 从 HandshakeRequest 获取客户端 IP（仅依赖标准 HTTP Header）
     * 优先级：X-Forwarded-For > X-Real-IP > 其他代理头
     */
    private String getClientIpFromHandshake(HandshakeRequest request) {
        try {
            Map<String, List<String>> headers = request.getHeaders();
            if (headers == null) {
                return "unknown";
            }

            // 1. 优先 X-Forwarded-For（支持多级代理）
            if (headers.containsKey("x-forwarded-for")) {
                String xff = headers.get("x-forwarded-for").get(0);
                if (isNotBlank(xff)) {
                    if (xff.contains(",")) {
                        return xff.split(",")[0].trim(); // 取最左边的原始客户端
                    }
                    return xff.trim();
                }
            }

            // 2. 尝试 X-Real-IP
            if (headers.containsKey("x-real-ip")) {
                String xri = headers.get("x-real-ip").get(0);
                if (isNotBlank(xri)) {
                    return xri.trim();
                }
            }

            // 3. 尝试其他代理头
            String[] proxyHeaders = {
                    "proxy-client-ip",
                    "wl-proxy-client-ip",
                    "http-client-ip"
            };

            for (String header : proxyHeaders) {
                if (headers.containsKey(header)) {
                    String ip = headers.get(header).get(0);
                    if (isNotBlank(ip)) {
                        return ip.trim();
                    }
                }
            }

            // 4. 标准 API 无法获取真实 IP，返回 unknown
            return "unknown";

        } catch (Exception e) {
            log.error("获取客户端 IP 失败", e);
            return "unknown";
        }
    }

    /**
     * 判断字符串是否为空或空白
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty() || "unknown".equalsIgnoreCase(str.trim());
    }

    private boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}