package cn.zdjc.wms.project.ai.service;

import cn.zdjc.wms.project.ai.dto.AiRequests;
import cn.zdjc.wms.project.ai.service.strategy.OrderCreateStrategy;
import cn.zdjc.wms.project.ai.service.strategy.OrderStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ChatBI 业务服务 - 使用策略模式重构
 * 重构前：361 行，包含大量重复的订单创建逻辑
 * 重构后：约 80 行，通过策略工厂委托给具体策略实现
 */
@Service
@Slf4j
public class ChatBIBusinessService {

    @Resource
    private OrderStrategyFactory orderStrategyFactory;

    /**
     * 创建移库单
     */
    @Transactional
    public Map<String, Object> createTransferOrder(String houseCode, String zoneName,
                                                    List<AiRequests.ChatBIItem> items) {
        log.info("ChatBI 创建移库单：houseCode={}, zoneName={}, items={}", houseCode, zoneName, items.size());
        
        OrderCreateStrategy strategy = orderStrategyFactory.getStrategy("TRANSFER");
        List<Map<String, Object>> itemMaps = convertItemsToMap(items);
        return strategy.createOrder(houseCode, zoneName, itemMaps);
    }

    /**
     * 创建盘点单
     */
    @Transactional
    public Map<String, Object> createStocktakeOrder(String houseCode, String zoneName,
                                                     List<AiRequests.ChatBIItem> items) {
        log.info("ChatBI 创建盘点单：houseCode={}, zoneName={}, items={}", houseCode, zoneName, items.size());
        
        OrderCreateStrategy strategy = orderStrategyFactory.getStrategy("STOCKTAKE");
        List<Map<String, Object>> itemMaps = convertItemsToMap(items);
        return strategy.createOrder(houseCode, zoneName, itemMaps);
    }

    /**
     * 创建 ASN 单（预留）
     */
    @Transactional
    public Map<String, Object> createAsnOrder(String houseCode, String zoneName,
                                               List<AiRequests.ChatBIItem> items) {
        log.info("ChatBI 创建 ASN 单：houseCode={}, zoneName={}, items={}", houseCode, zoneName, items.size());
        
        if (!orderStrategyFactory.supports("ASN")) {
            return buildMockResult("ASN", houseCode, zoneName, items.size());
        }
        
        OrderCreateStrategy strategy = orderStrategyFactory.getStrategy("ASN");
        List<Map<String, Object>> itemMaps = convertItemsToMap(items);
        return strategy.createOrder(houseCode, zoneName, itemMaps);
    }

    /**
     * 创建领料单（预留）
     */
    @Transactional
    public Map<String, Object> createRequisitionOrder(String houseCode, String zoneName,
                                                       List<AiRequests.ChatBIItem> items) {
        log.info("ChatBI 创建领料单：houseCode={}, zoneName={}, items={}", houseCode, zoneName, items.size());
        
        if (!orderStrategyFactory.supports("REQUISITION")) {
            return buildMockResult("REQUISITION", houseCode, zoneName, items.size());
        }
        
        OrderCreateStrategy strategy = orderStrategyFactory.getStrategy("REQUISITION");
        List<Map<String, Object>> itemMaps = convertItemsToMap(items);
        return strategy.createOrder(houseCode, zoneName, itemMaps);
    }

    /**
     * 创建托盘化单（预留）
     */
    @Transactional
    public Map<String, Object> createPalletizeOrder(String houseCode, String zoneName,
                                                     List<AiRequests.ChatBIItem> items) {
        log.info("ChatBI 创建托盘化单：houseCode={}, zoneName={}, items={}", houseCode, zoneName, items.size());
        
        if (!orderStrategyFactory.supports("PALLETIZE")) {
            return buildMockResult("PALLETIZE", houseCode, zoneName, items.size());
        }
        
        OrderCreateStrategy strategy = orderStrategyFactory.getStrategy("PALLETIZE");
        List<Map<String, Object>> itemMaps = convertItemsToMap(items);
        return strategy.createOrder(houseCode, zoneName, itemMaps);
    }

    /**
     * 将 ChatBIItem 转换为 Map
     */
    private List<Map<String, Object>> convertItemsToMap(List<AiRequests.ChatBIItem> items) {
        return items.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("skuCode", item.getSkuCode());
            map.put("skuName", item.getSkuName());
            map.put("batchNo", item.getBatchNo());
            map.put("availableQty", item.getAvailableQty());
            map.put("primaryUnit", item.getPrimaryUnit());
            map.put("locationCode", item.getLocationCode());
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 构建模拟结果（用于未实现的订单类型）
     */
    private Map<String, Object> buildMockResult(String orderType, String houseCode, 
                                                 String zoneName, int itemCount) {
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", "AI-" + orderType + "-" + System.currentTimeMillis() % 10000);
        result.put("status", "Created");
        result.put("message", orderType + "单创建成功（模拟数据）");
        result.put("houseCode", houseCode);
        result.put("zoneName", zoneName);
        result.put("itemCount", itemCount);
        return result;
    }
}
