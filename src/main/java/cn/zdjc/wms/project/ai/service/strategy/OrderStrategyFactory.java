package cn.zdjc.wms.project.ai.service.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单创建策略工厂 - 管理所有订单策略
 */
@Component
@Slf4j
public class OrderStrategyFactory {

    private final Map<String, OrderCreateStrategy> strategyMap = new ConcurrentHashMap<>();

    @Resource
    public void setStrategies(List<OrderCreateStrategy> strategies) {
        for (OrderCreateStrategy strategy : strategies) {
            strategyMap.put(strategy.getOrderType(), strategy);
            log.info("注册订单策略：{}", strategy.getOrderType());
        }
    }

    /**
     * 根据订单类型获取策略
     */
    public OrderCreateStrategy getStrategy(String orderType) {
        OrderCreateStrategy strategy = strategyMap.get(orderType.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的订单类型：" + orderType);
        }
        return strategy;
    }

    /**
     * 检查是否支持某种订单类型
     */
    public boolean supports(String orderType) {
        return strategyMap.containsKey(orderType.toUpperCase());
    }
}
