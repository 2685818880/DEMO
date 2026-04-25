package cn.zdjc.wms.project.ai.service;

import cn.zdjc.wms.project.ai.repository.inventory.InventoryRepository;
import cn.zdjc.wms.project.ai.repository.location.LocationRepository;
import cn.zdjc.wms.project.ai.repository.outbound.OutboundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AiDataService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AiDataServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private OutboundRepository outboundRepository;

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private AiDataService aiDataService;

    private List<Map<String, Object>> mockData;

    @BeforeEach
    void setUp() {
        mockData = new ArrayList<>();
        Map<String, Object> item = Map.of("sku_code", "SKU001", "total_qty", 100);
        mockData.add(item);
    }

    @Test
    void testGetCurrentStockSummary() {
        when(inventoryRepository.getCurrentStockSummary("WH001")).thenReturn(mockData);

        List<Map<String, Object>> result = aiDataService.getCurrentStockSummary("WH001");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SKU001", result.get(0).get("sku_code"));
        verify(inventoryRepository, times(1)).getCurrentStockSummary("WH001");
    }

    @Test
    void testGetWarehouseList() {
        when(locationRepository.getWarehouseList()).thenReturn(mockData);

        List<Map<String, Object>> result = aiDataService.getWarehouseList();

        assertNotNull(result);
        verify(locationRepository, times(1)).getWarehouseList();
    }

    @Test
    void testQuerySlowMovingProducts() {
        when(inventoryRepository.querySlowMovingProducts(anyString(), anyInt())).thenReturn(mockData);

        List<Map<String, Object>> result = aiDataService.querySlowMovingProducts("%", 30);

        assertNotNull(result);
        verify(inventoryRepository, times(1)).querySlowMovingProducts("%", 30);
    }

    @Test
    void testGetPickOutboundSummary() {
        when(outboundRepository.getPickOutboundSummary("WH001", 90)).thenReturn(mockData);

        List<Map<String, Object>> result = aiDataService.getPickOutboundSummary("WH001", 90);

        assertNotNull(result);
        verify(outboundRepository, times(1)).getPickOutboundSummary("WH001", 90);
    }
}
