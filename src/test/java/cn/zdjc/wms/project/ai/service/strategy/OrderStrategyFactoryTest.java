package cn.zdjc.wms.project.ai.service.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * OrderStrategyFactory 单元测试
 */
@ExtendWith(MockitoExtension.class)
class OrderStrategyFactoryTest {

    @InjectMocks
    private OrderStrategyFactory orderStrategyFactory;

    @Mock
    private OrderCreateStrategy transferStrategy;

    @Mock
    private OrderCreateStrategy stocktakeStrategy;

    @BeforeEach
    void setUp() {
        when(transferStrategy.getOrderType()).thenReturn("TRANSFER");
        when(stocktakeStrategy.getOrderType()).thenReturn("STOCKTAKE");
        
        List<OrderCreateStrategy> strategies = Arrays.asList(transferStrategy, stocktakeStrategy);
        orderStrategyFactory.setStrategies(strategies);
    }

    @Test
    void testGetStrategy_Transfer() {
        OrderCreateStrategy strategy = orderStrategyFactory.getStrategy("TRANSFER");
        
        assertNotNull(strategy);
        assertEquals(transferStrategy, strategy);
    }

    @Test
    void testGetStrategy_Stocktake() {
        OrderCreateStrategy strategy = orderStrategyFactory.getStrategy("stocktake");
        
        assertNotNull(strategy);
        assertEquals(stocktakeStrategy, strategy);
    }

    @Test
    void testGetStrategy_NotSupported() {
        assertThrows(IllegalArgumentException.class, () -> {
            orderStrategyFactory.getStrategy("UNKNOWN");
        });
    }

    @Test
    void testSupports() {
        assertTrue(orderStrategyFactory.supports("TRANSFER"));
        assertTrue(orderStrategyFactory.supports("stocktake"));
        assertFalse(orderStrategyFactory.supports("UNKNOWN"));
    }
}
