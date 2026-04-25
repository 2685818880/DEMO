package cn.zdjc.wms.project.ai.service.strategy;

import java.util.List;
import java.util.Map;

/**
 * ChatBI 订单创建策略接口
 */
public interface OrderCreateStrategy {
    
    /**
     * 支持的订单类型
     */
    String getOrderType();
    
    /**
     * 创建订单
     * @param houseCode 仓库编码
     * @param zoneName 区域名称
     * @param items 订单明细
     * @return 订单结果
     */
    Map<String, Object> createOrder(String houseCode, String zoneName, List<Map<String, Object>> items);
}
