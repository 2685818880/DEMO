package cn.zdjc.wms.project.domain.dto.ws;

import cn.zdjc.wms.customize.domain.dto.ExtraDto;
import cn.zdjc.wms.project.domain.enums.ws.WorkstationIsOpenEnums;
import cn.zdjc.wms.project.domain.enums.ws.WorkstationStatusEnums;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.foeris.y.common.bean.BeanAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.regex.Pattern;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkstationExtDto extends ExtraDto {

    /**
     * 工作站编号
     */
    private String workstationCode;

    /**
     * 工作站名称
     */
    private String workstationName;

    /**
     * 工作站描述
     */
    private String workstationDescribe;

    /**
     * 是否开启
     */
    private String isOpen = WorkstationIsOpenEnums.FALSE.getCode();

    /**
     * IP地址（支持IPv4/IPv6，可含端口）
     */
    private String workstationIp;

    /**
     * 工作站状态
     * 枚举: WorkstationStatusEnums
     */
    private String workstationStatus = WorkstationStatusEnums.IDLE.getCode();

    /**
     * 工作站模式
     * 枚举: WorkstationModeEnums
     */
    private String workstationMode;

    /**
     * 拣选站台左边点位
     */
    private String pickLeftDeviceCode;

    /**
     * 拣选站台右边点位
     */
    private String pickRightDeviceCode;

    /**
     * 理货站台左边点位
     */
    private String tallyLeftDeviceCode;

    /**
     * 理货站台右边点位
     */
    private String tallyRightDeviceCode;

    /**
     * 盘点站台左边点位
     */
    private String takeStockLeftDeviceCode;

    /**
     * 工作站拥有的模式
     */
    private List<String> workstationModeList;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 订单号
     */
    private String orderNo;



    private String orderId;

    // ====== IP地址校验正则表达式 ======
    /**
     * IPv4 格式: 0.0.0.0 ~ 255.255.255.255
     */
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$"
    );

    /**
     * IPv6 格式（简化版，支持标准/压缩格式）
     */
    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "^(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|"
                    + "([0-9a-fA-F]{1,4}:){1,7}:|"
                    + "([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|"
                    + "([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|"
                    + "([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|"
                    + "([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|"
                    + "([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|"
                    + "[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|"
                    + ":((:[0-9a-fA-F]{1,4}){1,7}|:)|"
                    + "fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|"
                    + "::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|"
                    + "([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))$"
    );

    /**
     * IP + 端口 格式: 192.168.1.1:8080 或 [2001:db8::1]:8080
     */
    private static final Pattern IP_WITH_PORT_PATTERN = Pattern.compile(
            "^(" +
                    "((" + IPV4_PATTERN.pattern() + ")|(" +
                    "\\[(" + IPV6_PATTERN.pattern() + ")\\]" +
                    "))" +
                    ":([0-9]{1,5})" +
                    ")$"
    );

    /**
     * 新增数据校验
     */
    public void checkDataAdd() {
        if (StringUtils.isEmpty(workstationCode)) {
            throw new IllegalStateException("工作站编号不能为空!");
        }
        if (StringUtils.isEmpty(workstationName)) {
            throw new IllegalStateException("工作站名称不能为空!");
        }
        if (StringUtils.isEmpty(workstationIp)) {
            throw new IllegalStateException("工作站IP不能为空!");
        }
        if (!isValidIp(workstationIp)) {
            throw new IllegalStateException("工作站IP格式不正确! 有效格式: IPv4(192.168.1.1)、IPv6(2001:db8::1) 或 含端口(192.168.1.1:8080)");
        }
    }

    /**
     * 更新状态数据校验
     */
    public void checkDataUpdateStatus() {
        if (StringUtils.isEmpty(workstationCode)) {
            throw new IllegalStateException("工作站编号不能为空!");
        }
        if (StringUtils.isEmpty(isOpen)) {
            throw new IllegalStateException("是否开启不能为空!");
        }
        if (StringUtils.isEmpty(workstationStatus)) {
            throw new IllegalStateException("工作站状态不能为空!");
        }
        if (StringUtils.isEmpty(workstationMode)) { // 修复原代码中的语法错误：workstationIpif → if
            throw new IllegalStateException("工作站模式不能为空!");
        }
        // 状态更新时如IP有值则校验格式
        if (!StringUtils.isEmpty(workstationIp) && !isValidIp(workstationIp)) {
            throw new IllegalStateException("工作站IP格式不正确! 有效格式: IPv4(192.168.1.1)、IPv6(2001:db8::1) 或 含端口(192.168.1.1:8080)");
        }
    }

    /**
     * 校验IP地址格式（支持IPv4/IPv6/含端口）
     *
     * @param ip IP地址字符串
     * @return true-有效IP，false-无效
     */
    private boolean isValidIp(String ip) {
        if (StringUtils.isEmpty(ip)) {
            return false;
        }

        // 场景1: 含端口 (192.168.1.1:8080 或 [2001:db8::1]:8080)
        if (IP_WITH_PORT_PATTERN.matcher(ip).matches()) {
            String portPart = ip.substring(ip.lastIndexOf(':') + 1);
            int port = Integer.parseInt(portPart);
            return port >= 1 && port <= 65535;
        }

        // 场景2: 纯IPv4
        if (IPV4_PATTERN.matcher(ip).matches()) {
            return true;
        }

        // 场景3: 纯IPv6（需排除端口干扰）
        if (ip.startsWith("[") && ip.endsWith("]")) {
            String ipv6 = ip.substring(1, ip.length() - 1);
            return IPV6_PATTERN.matcher(ipv6).matches();
        }

        // 场景4: 标准IPv6
        return IPV6_PATTERN.matcher(ip).matches();
    }

    /**
     * 获取纯IP地址（剥离端口）
     * 例如: "192.168.1.1:8080" → "192.168.1.1"
     *       "[2001:db8::1]:8080" → "2001:db8::1"
     */
    public String getPureIp() {
        if (StringUtils.isEmpty(workstationIp)) {
            return null;
        }

        // 处理 [IPv6]:port 格式
        if (workstationIp.startsWith("[") && workstationIp.contains("]:")) {
            int endBracket = workstationIp.indexOf(']');
            return workstationIp.substring(1, endBracket);
        }

        // 处理 IPv4:port 格式
        if (workstationIp.contains(":") && !workstationIp.contains("::")) { // 排除IPv6的双冒号
            int lastColon = workstationIp.lastIndexOf(':');
            // 检查冒号后是否为数字（端口）
            String afterColon = workstationIp.substring(lastColon + 1);
            if (afterColon.matches("\\d+")) {
                return workstationIp.substring(0, lastColon);
            }
        }

        return workstationIp; // 无端口直接返回
    }

    /**
     * 获取端口号（如存在）
     *
     * @return 端口号，无端口时返回 null
     */
    public Integer getPort() {
        if (StringUtils.isEmpty(workstationIp)) {
            return null;
        }

        // 处理 [IPv6]:port 格式
        if (workstationIp.startsWith("[") && workstationIp.contains("]:")) {
            int endBracket = workstationIp.indexOf(']');
            if (endBracket < workstationIp.length() - 1) {
                String portPart = workstationIp.substring(endBracket + 2); // 跳过 "]:"
                try {
                    int port = Integer.parseInt(portPart);
                    return (port >= 1 && port <= 65535) ? port : null;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        // 处理 IPv4:port 格式
        if (workstationIp.contains(":") && !workstationIp.contains("::")) {
            int lastColon = workstationIp.lastIndexOf(':');
            String portPart = workstationIp.substring(lastColon + 1);
            try {
                int port = Integer.parseInt(portPart);
                return (port >= 1 && port <= 65535) ? port : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }
}