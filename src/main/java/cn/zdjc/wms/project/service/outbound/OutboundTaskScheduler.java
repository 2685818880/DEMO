package cn.zdjc.wms.project.service.outbound;

import cn.zdjc.bm.transjob.core.DispatchManager;
import cn.zdjc.bm.transjob.core.dispatch.Dispatch;
import cn.zdjc.bm.transjob.core.dispatch.param.DispatchParam;
import cn.zdjc.bm.transjob.domain.dto.DispatchInfoDto;
import cn.zdjc.bm.transjob.domain.dto.DispatchJobDto;
import cn.zdjc.bm.transjob.domain.dto.DoDispatchDto;
import cn.zdjc.bm.transjob.domain.entity.DispatchJob;
import cn.zdjc.bm.transjob.domain.service.DispatchInfoService;
import cn.zdjc.bm.transjob.domain.service.DispatchJobService;
import cn.zdjc.warehouse.WmsConfig;
import cn.zdjc.wms.api.service.impl.PublishJobTaskAppServiceImpl;
import cn.zdjc.wms.core.dispatch.TransportDispatch;
import cn.zdjc.wms.core.dispatch.param.TransportDispatchParam;
import cn.zdjc.wms.core.callback.TransportCallback;
import cn.zdjc.wms.core.callback.param.TransportCallbackParam;
import cn.zdjc.wms.project.domain.entity.outbound.OutboundExtEntity;
import cn.zdjc.wms.project.mapper.outbound.OutboundExtMapper;
import cn.zdjc.wms.transport.out.infrastructure.enums.OutBoundStatus;
import com.foeris.y.common.json.JsonUtl;
import com.foreris.eris.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * 出库任务下发定时器服务
 * 
 * 定时检查待下发的出库任务并自动下发到WCS系统
 * 
 * @author Cline
 * @since 2026-02-27
 */
@Slf4j
@Service
public class OutboundTaskScheduler {

    @Resource
    private OutboundExtMapper outboundExtMapper;
    
    @Resource
    private DispatchJobService dispatchJobService;
    
    @Resource
    private DispatchInfoService dispatchInfoService;
    
    @Resource
    private DispatchManager dispatchManager;
    
    @Resource
    private PublishJobTaskAppServiceImpl publishJobTaskAppService;
    
    /**
     * 定时检查并下发出库任务
     * 每30秒执行一次
     * 
     * 业务流程：
     * 1. 查询状态为"就绪"的出库任务
     * 2. 检查容器是否已有活跃任务
     * 3. 创建调度任务并下发到WCS
     */
//    @Scheduled(fixedDelay = 30000) // 每30秒执行一次
    @Transactional(rollbackFor = Exception.class)
    public void scheduleOutboundTasks() {
        try {
            log.info("【出库任务定时器】开始检查待下发的出库任务...");
            
            // 1. 查询状态为"就绪"的出库任务
            List<OutboundExtEntity> readyOutbounds = queryReadyOutbounds();
            
            if (readyOutbounds.isEmpty()) {
                log.info("【出库任务定时器】没有待下发的出库任务");
                return;
            }
            
            log.info("【出库任务定时器】找到 {} 个待下发的出库任务", readyOutbounds.size());
            
            // 2. 处理每个待下发的出库任务
            int successCount = 0;
            int failCount = 0;
            
            for (OutboundExtEntity outbound : readyOutbounds) {
                try {
                    boolean result = dispatchOutboundTask(outbound);
                    if (result) {
                        successCount++;
                        log.info("【出库任务定时器】出库任务下发成功: formNo={}, containerCode={}", 
                                outbound.getFormNo(), outbound.getContainerCode());
                    } else {
                        failCount++;
                        log.warn("【出库任务定时器】出库任务下发失败: formNo={}, containerCode={}", 
                                outbound.getFormNo(), outbound.getContainerCode());
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("【出库任务定时器】出库任务下发异常: formNo={}, containerCode={}, error={}", 
                            outbound.getFormNo(), outbound.getContainerCode(), e.getMessage(), e);
                }
            }
            
            log.info("【出库任务定时器】处理完成: 成功={}, 失败={}, 总计={}", 
                    successCount, failCount, readyOutbounds.size());
            
        } catch (Exception e) {
            log.error("【出库任务定时器】定时任务执行异常", e);
        }
    }
    
    /**
     * 查询状态为"就绪"的出库任务
     */
    private List<OutboundExtEntity> queryReadyOutbounds() {
        try {
            // 查询状态为"就绪"的出库任务
            // 根据代码分析，Created状态对应"就绪"状态
            List<OutboundExtEntity> outbounds = outboundExtMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OutboundExtEntity>()
                    .eq("form_status", OutBoundStatus.Created.name())
                    .isNotNull("container_code")
                    .isNotNull("location_code")
                    .orderByAsc("create_datetime")
            );
            
            // 过滤掉容器已有活跃任务的任务
            return filterOutboundsWithActiveJobs(outbounds);
            
        } catch (Exception e) {
            log.error("【出库任务定时器】查询出库任务异常", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 过滤掉容器已有活跃任务的任务
     */
    private List<OutboundExtEntity> filterOutboundsWithActiveJobs(List<OutboundExtEntity> outbounds) {
        if (outbounds == null || outbounds.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<OutboundExtEntity> filtered = new ArrayList<>();
        String houseCode = WmsConfig.DefaultHouseCode;
        
        for (OutboundExtEntity outbound : outbounds) {
            String containerCode = outbound.getContainerCode();
            if (containerCode == null || containerCode.trim().isEmpty()) {
                continue;
            }
            
            // 检查容器是否已有活跃任务
            DispatchJobDto activeJob = dispatchJobService.queryActiveJob(houseCode, containerCode);
            if (activeJob == null) {
                filtered.add(outbound);
            } else {
                log.debug("【出库任务定时器】容器已有活跃任务，跳过: containerCode={}, jobId={}", 
                        containerCode, activeJob.getId());
            }
        }
        
        return filtered;
    }
    
    /**
     * 下发出库任务
     */
    private boolean dispatchOutboundTask(OutboundExtEntity outbound) {
        String houseCode = WmsConfig.DefaultHouseCode;
        String containerCode = outbound.getContainerCode();
        String locationCode = outbound.getLocationCode();
        String outStation = outbound.getOutStation();
        
        try {
            log.info("【出库任务下发】开始下发任务: formNo={}, containerCode={}, locationCode={}, outStation={}", 
                    outbound.getFormNo(), containerCode, locationCode, outStation);
            
            // 1. 检查容器是否已有活跃任务（双重检查）
            DispatchJobDto activeJob = dispatchJobService.queryActiveJob(houseCode, containerCode);
            if (activeJob != null) {
                log.warn("【出库任务下发】容器已有活跃任务，跳过下发: containerCode={}, jobId={}", 
                        containerCode, activeJob.getId());
                return false;
            }
            
            // 2. 创建调度信息
            String dispatchCode = TransportDispatch.class.getSimpleName();
            String callbackCode = TransportCallback.class.getSimpleName();
            
            TransportDispatchParam dispatchParam = new TransportDispatchParam();
            dispatchParam.setHouseCode(houseCode);
            dispatchParam.setContainerCode(containerCode);
            dispatchParam.setLocFrom(locationCode); // 从原库位出发
            dispatchParam.setLocTo(outStation != null ? outStation : "OUTBOUND_STATION"); // 到下架站台
            dispatchParam.setAuto_send(true);
            
            TransportCallbackParam callbackParam = new TransportCallbackParam();
            
            // 3. 创建调度信息
            DispatchInfoDto dispatchInfo = dispatchInfoService.createDispatchInfoWithReturn(
                    houseCode, 
                    containerCode, 
                    "", 
                    "", 
                    dispatchCode, 
                    dispatchParam, 
                    callbackCode, 
                    callbackParam, 
                    ""
            );
            
            if (dispatchInfo == null) {
                log.error("【出库任务下发】创建调度信息失败: formNo={}, containerCode={}", 
                        outbound.getFormNo(), containerCode);
                return false;
            }
            
            // 4. 执行调度
            List<DispatchJob> dispatchJobs = executeDispatch(dispatchInfo, dispatchParam);
            
            if (ObjectUtils.isEmpty(dispatchJobs)) {
                log.error("【出库任务下发】执行调度失败，未生成调度任务: formNo={}, containerCode={}", 
                        outbound.getFormNo(), containerCode);
                return false;
            }
            
            // 5. 发布任务到WCS
            for (DispatchJob dispatchJob : dispatchJobs) {
                // 使用BeanUtl复制属性创建DispatchJobDto
                DispatchJobDto jobDto = com.foeris.y.common.bean.BeanUtl.copyProperties(dispatchJob, DispatchJobDto.class);
                jobDto.setParameters(new HashMap<>());
                
                // 更新出库任务状态为"执行中"
                updateOutboundStatus(outbound, OutBoundStatus.Executing);
                
                // 发布任务到WCS
                publishJobTaskAppService.publishDispatchJobToWCS(jobDto);
                
                log.info("【出库任务下发】任务发布成功: formNo={}, jobId={}", 
                        outbound.getFormNo(), dispatchJob.getId());
            }
            
            return true;
            
        } catch (BusinessException e) {
            log.error("【出库任务下发】业务异常: formNo={}, containerCode={}, error={}", 
                    outbound.getFormNo(), containerCode, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("【出库任务下发】系统异常: formNo={}, containerCode={}, error={}", 
                    outbound.getFormNo(), containerCode, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 执行调度
     */
    private List<DispatchJob> executeDispatch(DispatchInfoDto dispatchInfo, TransportDispatchParam dispatchParam) {
        try {
            Dispatch dispatch = dispatchManager.queryByCode(dispatchInfo.getDispatch_code());
            if (dispatch == null) {
                log.error("【出库任务下发】调度器未找到: dispatchCode={}", dispatchInfo.getDispatch_code());
                return Collections.emptyList();
            }
            
            DoDispatchDto doDispatchDto = new DoDispatchDto(
                    dispatchInfo.getId(),
                    dispatchInfo.getHouse_code(),
                    dispatchInfo.getContainer_code(),
                    dispatchParam.getLocFrom(),
                    dispatchParam.getLocTo(),
                    dispatchInfo.getContainerShape(),
                    dispatchInfo.getParameters()
            );
            
            String paramJson = new String(dispatchInfo.getDispatch_param());
            DispatchParam param = (DispatchParam) JsonUtl.format(paramJson, dispatch.getActionParamClass());
            
            return dispatch.doDispatch(doDispatchDto, param, true);
            
        } catch (Exception e) {
            log.error("【出库任务下发】执行调度异常", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 更新出库任务状态
     */
    private void updateOutboundStatus(OutboundExtEntity outbound, OutBoundStatus status) {
        try {
            outbound.setFormStatus(status);
            outbound.setOutStartDatetime(new Date());
            outbound.setLastModifyDatetime(new Date());
            outbound.setLastModifyBy("SYSTEM_SCHEDULER");
            
            outboundExtMapper.updateById(outbound);
            
            log.info("【出库任务状态更新】成功: formNo={}, status={}", outbound.getFormNo(), status);
        } catch (Exception e) {
            log.error("【出库任务状态更新】失败: formNo={}, status={}, error={}", 
                    outbound.getFormNo(), status, e.getMessage(), e);
        }
    }
    
    /**
     * 手动触发出库任务下发（用于测试或手动触发）
     */
    public boolean triggerManualDispatch(String formNo) {
        try {
            OutboundExtEntity outbound = outboundExtMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OutboundExtEntity>()
                    .eq("form_no", formNo)
            );
            
            if (outbound == null) {
                log.error("【手动触发】出库任务不存在: formNo={}", formNo);
                return false;
            }
            
            if (outbound.getFormStatus() != OutBoundStatus.Created) {
                log.error("【手动触发】出库任务状态不是就绪状态: formNo={}, status={}", 
                        formNo, outbound.getFormStatus());
                return false;
            }
            
            return dispatchOutboundTask(outbound);
            
        } catch (Exception e) {
            log.error("【手动触发】出库任务下发异常: formNo={}", formNo, e);
            return false;
        }
    }
}