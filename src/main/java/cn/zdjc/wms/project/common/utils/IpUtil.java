package cn.zdjc.wms.project.common.utils;

import org.springframework.util.StringUtils;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

public class IpUtil {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";

    // IP地址正则表达式（支持IPv4和IPv6）
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$|" +
                    "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$"
    );

    /**
     * 获取客户端真实IP地址
     *
     * @param request HTTP请求对象
     * @return 客户端真实IP地址，如果无法获取则返回null
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        // 1. 尝试从X-Forwarded-For获取（支持多层代理）
        String ip = getIpFromHeader(request, "X-Forwarded-For");
        if (isValidIp(ip)) {
            // X-Forwarded-For格式：client, proxy1, proxy2
            // 第一个IP是客户端真实IP
            String[] ips = ip.split(",");
            for (String candidate : ips) {
                candidate = candidate.trim();
                if (isValidIp(candidate)) {
                    return candidate;
                }
            }
        }

        // 2. 尝试从其他常见代理头获取
        String[] headers = {
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headers) {
            ip = getIpFromHeader(request, header);
            if (isValidIp(ip)) {
                return ip;
            }
        }

        // 3. 最后从RemoteAddr获取
        ip = request.getRemoteAddr();
        return isValidIp(ip) ? ip : null;
    }

    /**
     * 从请求头中获取IP地址
     */
    private static String getIpFromHeader(HttpServletRequest request, String headerName) {
        try {
            String ip = request.getHeader(headerName);
            return StringUtils.hasText(ip) ? ip.trim() : null;
        } catch (Exception e) {
            // 记录异常日志（如果需要）
            return null;
        }
    }

    /**
     * 验证IP地址的有效性
     *
     * @param ip IP地址字符串
     * @return true表示有效，false表示无效
     */
    private static boolean isValidIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return false;
        }

        ip = ip.trim();

        // 排除无效值（允许127.0.0.1作为有效IP）
        if (UNKNOWN.equalsIgnoreCase(ip) ||
                LOCALHOST_IPV6.equals(ip)) {
            return false;
        }

        // IP格式验证
        return IP_PATTERN.matcher(ip).matches();
    }

    /**
     * 判断是否为内网IP
     *
     * @param ip IP地址
     * @return true表示内网IP
     */
    public static boolean isInnerIp(String ip) {
        if (!isValidIp(ip)) {
            return false;
        }

        try {
            String[] parts = ip.split("\\.");
            if (parts.length == 4) {
                int first = Integer.parseInt(parts[0]);
                int second = Integer.parseInt(parts[1]);

                // 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16
                return first == 10 ||
                        (first == 172 && second >= 16 && second <= 31) ||
                        (first == 192 && second == 168);
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    /**
     * 获取IP地址的简化版本（隐藏部分信息用于日志）
     *
     * @param ip IP地址
     * @return 简化后的IP地址
     */
    public static String getMaskedIp(String ip) {
        if (!isValidIp(ip)) {
            return "invalid_ip";
        }

        try {
            if (ip.contains(":")) {
                // IPv6简化
                return ip.substring(0, ip.indexOf(":") + 1) + "****";
            } else {
                // IPv4简化（保留前两段）
                String[] parts = ip.split("\\.");
                return parts[0] + "." + parts[1] + ".*.*";
            }
        } catch (Exception e) {
            return "masked_ip";
        }
    }
}