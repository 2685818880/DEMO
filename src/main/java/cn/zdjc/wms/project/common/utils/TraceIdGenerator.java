package cn.zdjc.wms.project.common.utils;

import cn.zdjc.wms.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TraceId 生成工具类（增强版）
 */
@Slf4j
@Component
public class TraceIdGenerator {
    
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    private final String machineId;
    private final AtomicInteger sequence = new AtomicInteger(0);
    private volatile long lastSecond = -1;
    
    public TraceIdGenerator(@Value("${machine.id:0000}") String configMachineId) {
        // 优先使用配置，否则自动计算
        this.machineId = validateMachineId(configMachineId);
    }
    
    /**
     * 生成 traceId
     * 格式：yyyyMMddHHmmss + machineId(4) + seq(4) = 22 位
     */
    public String generate() {
        // 1. 时间戳
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        
        // 2. 序列号（秒内自增，线程安全）
        String seq = nextSequence();
        
        return timestamp + machineId + seq;
    }
    
    /**
     * 获取/生成机器标识（4 位十六进制）
     */
    private String validateMachineId(String configId) {
        // 1. 优先使用配置
        if (StringUtils.isNotBlank(configId) && configId.matches("^[0-9a-fA-F]{4}$")) {
            return configId.toLowerCase();
        }
        
        // 2. 自动计算：IP 后 2 段 → 4 位十六进制
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            String[] parts = ip.split("\\.");
            if (parts.length >= 4) {
                int val = (Integer.parseInt(parts[2]) << 8) | Integer.parseInt(parts[3]);
                return String.format("%04x", val & 0xFFFF);
            }
        } catch (Exception e) {
            log.warn("自动计算 machineId 失败，使用默认值", e);
        }
        
        // 3. 兜底：随机值
        return String.format("%04x", new Random().nextInt(0xFFFF));
    }
    
    /**
     * 生成秒内序列号（0000-0999，线程安全）
     */
    private synchronized String nextSequence() {
        long currentSecond = System.currentTimeMillis() / 1000;
        if (currentSecond != lastSecond) {
            sequence.set(0);  // 新秒重置
            lastSecond = currentSecond;
        }
        // 保证 4 位，超过 999 时循环（极端情况概率极低）
        return String.format("%04d", sequence.getAndIncrement() % 1000);
    }
    
    /**
     * 解析 traceId 中的时间（用于日志检索辅助）
     */
    public static LocalDateTime parseTime(String traceId) {
        if (traceId == null || traceId.length() < 14) return null;
        try {
            return LocalDateTime.parse(traceId.substring(0, 14), TIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
}