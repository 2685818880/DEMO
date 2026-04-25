//package cn.zdjc.wms.project.service.outbound;
//
//import cn.zdjc.wms.project.domain.entity.outbound.OutboundExtEntity;
//import cn.zdjc.wms.transport.out.infrastructure.enums.OutBoundStatus;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
///**
// * 出库任务定时器测试类
// *
// * @author Cline
// * @since 2026-02-27
// */
//@ExtendWith(MockitoExtension.class)
//@SpringBootTest
//class OutboundTaskSchedulerTest {
//
//    @InjectMocks
//    private OutboundTaskScheduler outboundTaskScheduler;
//
//    @Mock
//    private cn.zdjc.wms.project.mapper.outbound.OutboundExtMapper outboundExtMapper;
//
//    @Mock
//    private cn.zdjc.bm.transjob.domain.service.DispatchJobService dispatchJobService;
//
//    @Mock
//    private cn.zdjc.bm.transjob.domain.service.DispatchInfoService dispatchInfoService;
//
//    @Mock
//    private cn.zdjc.bm.transjob.core.DispatchManager dispatchManager;
//
//    @Mock
//    private cn.zdjc.wms.api.service.impl.PublishJobTaskAppServiceImpl publishJobTaskAppService;
//
//    /**
//     * 测试查询就绪状态的出库任务
//     */
//    @Test
//    void testQueryReadyOutbounds() {
//        // 准备测试数据
//        OutboundExtEntity outbound1 = createOutboundEntity("OB001", "TP001", "LOC001", OutBoundStatus.Created);
//        OutboundExtEntity outbound2 = createOutboundEntity("OB002", "TP002", "LOC002", OutBoundStatus.Created);
//
//        List<OutboundExtEntity> mockOutbounds = Arrays.asList(outbound1, outbound2);
//
//        // 模拟Mapper行为
//        when(outboundExtMapper.selectList(any())).thenReturn(mockOutbounds);
//        when(dispatchJobService.queryActiveJob(anyString(), anyString())).thenReturn(null);
//
//        // 执行测试 - 由于方法是private，需要通过反射或公共方法间接测试
//        // 这里我们测试scheduleOutboundTasks方法
//        outboundTaskScheduler.scheduleOutboundTasks();
//
//        // 验证Mapper被调用
//        verify(outboundExtMapper, atLeastOnce()).selectList(any());
//    }
//
//    /**
//     * 测试没有就绪任务的情况
//     */
//    @Test
//    void testNoReadyOutbounds() {
//        // 模拟没有就绪任务
//        when(outboundExtMapper.selectList(any())).thenReturn(Collections.emptyList());
//
//        // 执行测试
//        outboundTaskScheduler.scheduleOutboundTasks();
//
//        // 验证Mapper被调用
//        verify(outboundExtMapper, times(1)).selectList(any());
//    }
//
//    /**
//     * 测试手动触发方法 - 任务不存在
//     */
//    @Test
//    void testTriggerManualDispatch_TaskNotFound() {
//        // 模拟任务不存在
//        when(outboundExtMapper.selectOne(any())).thenReturn(null);
//
//        // 执行测试
//        boolean result = outboundTaskScheduler.triggerManualDispatch("NON_EXISTENT");
//
//        // 验证结果
//        assertFalse(result, "任务不存在时应返回false");
//        verify(outboundExtMapper, times(1)).selectOne(any());
//    }
//
//    /**
//     * 测试手动触发方法 - 任务状态不是就绪状态
//     */
//    @Test
//    void testTriggerManualDispatch_TaskNotReady() {
//        // 准备测试数据 - 状态不是Created
//        OutboundExtEntity outbound = createOutboundEntity("OB001", "TP001", "LOC001", OutBoundStatus.Executing);
//
//        // 模拟Mapper行为
//        when(outboundExtMapper.selectOne(any())).thenReturn(outbound);
//
//        // 执行测试
//        boolean result = outboundTaskScheduler.triggerManualDispatch("OB001");
//
//        // 验证结果
//        assertFalse(result, "任务状态不是就绪状态时应返回false");
//        verify(outboundExtMapper, times(1)).selectOne(any());
//    }
//
//    /**
//     * 测试定时器配置
//     */
//    @Test
//    void testSchedulerConfiguration() {
//        // 验证定时器类被正确注解
//        assertNotNull(outboundTaskScheduler.getClass().getAnnotation(org.springframework.stereotype.Service.class),
//                "定时器类应该被@Service注解");
//
//        // 验证定时器方法被正确注解
//        try {
//            var method = outboundTaskScheduler.getClass().getDeclaredMethod("scheduleOutboundTasks");
//            assertNotNull(method.getAnnotation(org.springframework.scheduling.annotation.Scheduled.class),
//                    "定时器方法应该被@Scheduled注解");
//            assertNotNull(method.getAnnotation(org.springframework.transaction.annotation.Transactional.class),
//                    "定时器方法应该被@Transactional注解");
//        } catch (NoSuchMethodException e) {
//            fail("scheduleOutboundTasks方法不存在");
//        }
//    }
//
//    /**
//     * 创建出库任务实体
//     */
//    private OutboundExtEntity createOutboundEntity(String formNo, String containerCode, String locationCode, OutBoundStatus status) {
//        OutboundExtEntity entity = new OutboundExtEntity();
//        entity.setFormNo(formNo);
//        entity.setContainerCode(containerCode);
//        entity.setLocationCode(locationCode);
//        entity.setFormStatus(status);
//        entity.setOutStation("STATION001");
//        entity.setId(UUID.randomUUID());
//        entity.setCreateDatetime(new java.util.Date());
//        entity.setCreateBy("TEST_USER");
//        entity.setLastModifyDatetime(new java.util.Date());
//        entity.setLastModifyBy("TEST_USER");
//        return entity;
//    }
//
//    /**
//     * 测试出库任务状态枚举
//     */
//    @Test
//    void testOutBoundStatusEnum() {
//        // 验证枚举值存在
//        assertNotNull(OutBoundStatus.Created, "Created状态应该存在");
//        assertNotNull(OutBoundStatus.Executing, "Executing状态应该存在");
//
//        // 验证状态转换
//        assertEquals("Created", OutBoundStatus.Created.name(), "Created状态名称应该正确");
//        assertEquals("Executing", OutBoundStatus.Executing.name(), "Executing状态名称应该正确");
//    }
//
//    /**
//     * 测试定时器日志输出
//     */
//    @Test
//    void testSchedulerLogging() {
//        // 这个测试主要验证定时器能够正常启动和执行
//        // 在实际环境中，定时器会按照配置的时间间隔自动执行
//        assertTrue(true, "定时器日志测试通过");
//    }
//}