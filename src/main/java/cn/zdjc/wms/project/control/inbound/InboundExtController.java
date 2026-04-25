package cn.zdjc.wms.project.control.inbound;

import cn.zdjc.platform.system.util.ExceptionUtlEx;
import cn.zdjc.warehouse.WmsConfig;
import cn.zdjc.warehouse.container.domain.dto.ContainerDto;
import cn.zdjc.wms.PlatFormConfig;
import cn.zdjc.wms.api.dto.TaskApplyParam;
import cn.zdjc.wms.api.service.TransJobAppService;
import cn.zdjc.wms.definition.infrastructure.enums.AsnStatus;
import cn.zdjc.wms.definition.infrastructure.enums.PalletizeFormStatus;
import cn.zdjc.wms.project.common.constant.ProjectConstant;
import cn.zdjc.wms.project.domain.dto.asn.AsnItemExtDto;
import cn.zdjc.wms.project.domain.dto.inbound.*;
import cn.zdjc.wms.project.domain.dto.pick.ContainerLeaveDto;
import cn.zdjc.wms.project.domain.query.asn.AsnItemExtExQuery;
import cn.zdjc.wms.project.domain.query.palletize.PalletizeItemExtQuery;
import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import cn.zdjc.wms.project.service.asn.AsnItemExtService;
import cn.zdjc.wms.project.service.inbound.PjInboundService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.foeris.y.common.json.JsonUtl;
import com.foeris.y.common.result.MessageResult;
import com.foeris.y.common.result.PageResult;
import com.foeris.y.common.result.ResultFactory;
import com.wxzd.wcs.platform_sdk.v3.wms.task.ContainerShape;
import com.wxzd.wcs.platform_sdk.v3.wms.task.TaskApplyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 入库相关控制器
 */
@RestController
@RequestMapping(ProjectConstant.PREFIX_CONTEXT_PATH + "/inbound")
@Slf4j
public class InboundExtController {

    @Resource
    private PjInboundService inboundService;

    @Resource
    private AsnItemExtService asnItemExtService;


    @Resource
    private TransJobAppService transJobAppService;

    /**
     * 收料单列表（不分页）
     */
    @PostMapping("/find-orders-list")
    public ResultWrapper<List<OrderExtDto>> findOrderListInfo(@RequestBody AsnItemExtExQuery query) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("【查询收料单分页】参数: {}", query);

            String workstationCode = query.getWorkstationCode();
            if (StringUtils.isEmpty(workstationCode)) {
                return ResultWrapper.buildFailure("工作站编码不能为空");
            }
            List<String> statusList = new ArrayList<>();
            statusList.add(String.valueOf(AsnStatus.Created));
            statusList.add(String.valueOf(AsnStatus.Executing));
            query.setAsnStatusList(statusList);

            // 调用服务层查询
            List<OrderExtDto> result = inboundService.orderNoInfoList(query);

            log.info("【查询收料单列表】返回 {} 个单号, 耗时: {}ms",
                    result != null ? result.size() : 0,
                    System.currentTimeMillis() - startTime);

            return ResultWrapper.buildSuccess(result);

        } catch (Exception e) {
            log.error("【查询收料单列表】系统异常, 耗时: {}ms, 参数: {}",
                    System.currentTimeMillis() - startTime,
                    query,
                    e);
            return ResultWrapper.buildFailure("查询失败: " + e.getMessage());
        }
    }

    /**
     * 收料单列表分页
     */
    @GetMapping("/find-orders-page")
    public PageResult<OrderExtDto> findOrderPageInfo(@RequestBody AsnItemExtExQuery query) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("【查询收料单分页】参数: {}", query);
            String workstationCode = query.getWorkstationCode();
            if (StringUtils.isEmpty(workstationCode)) {
                return ResultFactory.getErrorPage("工作站编码不能为空!");
            }
            List<String> statusList = new ArrayList<>();
            statusList.add(String.valueOf(AsnStatus.Created));
            statusList.add(String.valueOf(AsnStatus.Executing));
            query.setAsnStatusList(statusList);

            // 调用服务层查询
            PageResult<OrderExtDto> result = inboundService.orderNoPageInfo(query);

            log.info("【查询收料单分页】返回 {} 条, 总计 {} 条, 耗时: {}ms",
                    result != null && result.getRows() != null ? result.getRows().size() : 0,
                    result != null ? result.getTotal() : 0,
                    System.currentTimeMillis() - startTime);

            return result;

        } catch (Exception e) {
            log.error("【查询收料单分页】系统异常, 耗时: {}ms, 参数: {}",
                    System.currentTimeMillis() - startTime,
                    query,
                    e);
            return ResultFactory.getErrorPage("系统异常: " + e.getMessage());
        }
    }



    /**
     * 通过订单获取订单明细信息
     */
    @PostMapping("/find-item")
    public ResultWrapper<List<AsnItemExtDto>> findItemInfo(@RequestBody PutAwayCombineDto putAwayCombineDto) {
        try {
            if (putAwayCombineDto == null) {
                log.warn("【查询订单明细】收到空请求体");
                return ResultWrapper.buildFailure("请求体不能为空");
            }

            String businessOrderNo = putAwayCombineDto.getBusinessOrderNo();
            if (StringUtils.isEmpty(businessOrderNo)) {
                log.warn("【查询订单明细】业务单号为空");
                return ResultWrapper.buildFailure("订单号不能为空!");
            }

            log.info("【查询订单明细】收到请求，业务单号：{}", businessOrderNo);

            putAwayCombineDto.setSourceType(1);
            List<AsnItemExtDto> result = inboundService.findItemInfo(putAwayCombineDto);

            log.info("【查询订单明细】查询成功，业务单号：{}，找到 {} 条明细", businessOrderNo, result != null ? result.size() : 0);
            return ResultWrapper.buildSuccess(result);

        } catch (Exception e) {
            log.error("【查询订单明细】查询失败，业务单号：{}，错误：{}",
                    putAwayCombineDto != null ? putAwayCombineDto.getBusinessOrderNo() : "null",
                    e.getMessage(), e);
            return ResultWrapper.buildFailure("查询明细失败: " + e.getMessage());
        }
    }



    /**
     * 工作站有单据收货（单条上架组盘）
     */
//    @PostMapping("/put-away")
//    public ResultWrapper<Void> putAway(@RequestBody PutAwayCombineDto putAwayCombineDto) {
//        try {
//            if (putAwayCombineDto == null) {
//                log.warn("【单条上架】收到空请求体");
//                return ResultWrapper.buildFailure("请求体不能为空");
//            }
//
//            log.info("【单条上架】收到请求：业务单号={}, 容器编码={}, SKU编码={}",
//                    putAwayCombineDto.getBusinessOrderNo(),
//                    putAwayCombineDto.getContainerCode(),
//                    putAwayCombineDto.getSkuCode());
//
//            putAwayCombineDto.checkData();
//            inboundService.putAway(putAwayCombineDto);
//
//            log.info("【单条上架】处理成功，容器编码：{}", putAwayCombineDto.getContainerCode());
//            return ResultWrapper.buildSuccess(null);
//
//        } catch (Exception e) {
//            log.error("【单条上架】处理失败，容器编码：{}，错误：{}",
//                    putAwayCombineDto != null ? putAwayCombineDto.getContainerCode() : "null",
//                    e.getMessage(), e);
//            return ResultWrapper.buildFailure("上架失败: " + e.getMessage());
//        }
//    }

    /**
     * 工作站有单据批量收货
     */
    @PostMapping("/batch-put-away")
    public ResultWrapper<Void> batchPutAway(@RequestBody BatchPutAwayCombineDto batchPutAwayCombineDto) {
        try {
            if (batchPutAwayCombineDto == null) {
                log.warn("【组盘】收到空请求体");
                return ResultWrapper.buildFailure("请求体不能为空");
            }
            String workstationCode = batchPutAwayCombineDto.getWorkstationCode();
            if (StringUtils.isEmpty(workstationCode)) {
                return ResultWrapper.buildFailure("工作站编码不能为空!");
            }
            batchPutAwayCombineDto.setDeviceCode(workstationCode);

            int itemSize = batchPutAwayCombineDto.getItemList() != null
                    ? batchPutAwayCombineDto.getItemList().size() : 0;
            log.info("【组盘】收到请求：业务单号={}, 容器编码={}, 物料项数量={}",
                    batchPutAwayCombineDto.getBusinessOrderNo(),
                    batchPutAwayCombineDto.getContainerCode(),
                    itemSize);
            batchPutAwayCombineDto.setDeviceCode(batchPutAwayCombineDto.getWorkstationCode());
            batchPutAwayCombineDto.checkData();
            batchPutAwayCombineDto.setSourceType(1); // 标记来源为工作站
            inboundService.batchPutAway(batchPutAwayCombineDto);

            log.info("【组盘】处理成功，容器编码：{}", batchPutAwayCombineDto.getContainerCode());
            return ResultWrapper.buildSuccess(null);

        } catch (Exception e) {
            log.error("【组盘】处理失败，容器编码：{}，错误：{}",
                    batchPutAwayCombineDto != null ? batchPutAwayCombineDto.getContainerCode() : "null",
                    e.getMessage(), e);
            return ResultWrapper.buildFailure("组盘失败: " + e.getMessage());
        }
    }





    /**
     * 组盘解绑
     */
    @PostMapping("/combine-unbind")
    public ResultWrapper<Void> combineUnbind(@RequestBody BatchPutAwayCombineDto batchPutAwayCombineDto) {
        try {
            if (batchPutAwayCombineDto == null) {
                log.warn("【组盘解绑】收到空请求体");
                return ResultWrapper.buildFailure("请求体不能为空");
            }

            String containerCode = batchPutAwayCombineDto.getContainerCode();
            if (StringUtils.isEmpty(containerCode)) {
                log.warn("【组盘解绑】容器编码为空");
                return ResultWrapper.buildFailure("容器号不能为空!");
            }

            log.info("【组盘解绑】收到请求，容器编码：{}", containerCode);
            ResultWrapper<Void> voidResultWrapper = inboundService.combineUnbind(batchPutAwayCombineDto);
            if(!voidResultWrapper.isSuccess()){
                return voidResultWrapper;
            }

            log.info("【组盘解绑】处理成功，容器编码：{}", containerCode);
            return ResultWrapper.buildSuccess(null);

        } catch (Exception e) {
            log.error("【组盘解绑】处理失败，容器编码：{}，错误：{}",
                    batchPutAwayCombineDto != null ? batchPutAwayCombineDto.getContainerCode() : "null",
                    e.getMessage(), e);
            return ResultWrapper.buildFailure("解绑失败: " + e.getMessage());
        }
    }



    /**
     * 托盘（返回容器尺寸信息）
     */
    @PostMapping("/scan-tray")
    public ResultWrapper<ContainerDto> scanTray(@RequestBody PutAwayCombineDto putAwayCombineDto) {
        try {
            if (putAwayCombineDto == null) {
                log.warn("【扫描托盘】收到空请求体");
                return ResultWrapper.buildFailure("请求体不能为空");
            }

            String containerCode = putAwayCombineDto.getContainerCode();
            String businessOrderNo = putAwayCombineDto.getBusinessOrderNo();

            if (StringUtils.isEmpty(containerCode)) {
                log.warn("【扫描托盘】容器编码为空");
                return ResultWrapper.buildFailure("容器号不能为空!");
            }

            if (StringUtils.isEmpty(businessOrderNo)) {
                log.warn("【扫描托盘】业务单号为空");
                return ResultWrapper.buildFailure("订单号不能为空!");
            }

            log.info("【扫描托盘】收到请求：容器编码={}, 业务单号={}", containerCode, businessOrderNo);

            ContainerDto containerDto = inboundService.scanTray(putAwayCombineDto);

            log.info("【扫描托盘】验证通过，容器编码：{}", containerCode);
            return ResultWrapper.buildSuccess(containerDto);

        } catch (Exception e) {
            log.error("【扫描托盘】验证失败，容器编码：{}，业务单号：{}，错误：{}",
                    putAwayCombineDto != null ? putAwayCombineDto.getContainerCode() : "null",
                    putAwayCombineDto != null ? putAwayCombineDto.getBusinessOrderNo() : "null",
                    e.getMessage(), e);
            return ResultWrapper.buildFailure("扫描验证失败: " + e.getMessage());
        }
    }


    /**
     * 新到工装入库 无单据组盘
     */
    @PostMapping("/tooling-batch-put-away")
    public ResultWrapper<Void> toolingBatchPutAway(@RequestBody BatchPutAwayCombineDto batchPutAwayCombineDto) {
        try {
            if (batchPutAwayCombineDto == null) {
                log.warn("【组盘】收到空请求体");
                return ResultWrapper.buildFailure("请求体不能为空");
            }
            String workstationCode = batchPutAwayCombineDto.getWorkstationCode();
            if (StringUtils.isEmpty(workstationCode)) {
                return ResultWrapper.buildFailure("工作站编码不能为空!");
            }
            batchPutAwayCombineDto.setDeviceCode(workstationCode);
            int itemSize = batchPutAwayCombineDto.getItemList() != null
                    ? batchPutAwayCombineDto.getItemList().size() : 0;
            log.info("【组盘】收到请求：业务单号={}, 容器编码={}, 物料项数量={}",
                    batchPutAwayCombineDto.getBusinessOrderNo(),
                    batchPutAwayCombineDto.getContainerCode(),
                    itemSize);

            batchPutAwayCombineDto.checkData();
            batchPutAwayCombineDto.setSourceType(1);
            inboundService.toolingBatchPutAway(batchPutAwayCombineDto);

            log.info("【组盘】处理成功，容器编码：{}", batchPutAwayCombineDto.getContainerCode());
            return ResultWrapper.buildSuccess(null);

        } catch (Exception e) {
            log.error("【组盘】处理失败，容器编码：{}，错误：{}",
                    batchPutAwayCombineDto != null ? batchPutAwayCombineDto.getContainerCode() : "null",
                    e.getMessage(), e);
            return ResultWrapper.buildFailure("组盘失败: " + e.getMessage());
        }
    }
    /**
     * 【工作站】获取已组盘工装容器信息
     */
    @PostMapping("/tooling-container-info")
    public ResultWrapper<ToolingContainerInfoDto> toolingContainerInfo(@RequestBody BatchPutAwayCombineDto batchPutAwayCombineDto) {
        try {
            ToolingContainerInfoDto request = new ToolingContainerInfoDto();
            PalletizeItemExtQuery query=new PalletizeItemExtQuery();
            query.setPalletizeFormStatusList(Arrays.asList(PalletizeFormStatus.Finished,PalletizeFormStatus.Closed));
            List<ToolingContainerDto> containerList = inboundService.palletizeItemExtEntities(query);
            request.setContainerList(containerList);
            return ResultWrapper.buildSuccess(request);

        } catch (Exception e) {

            return ResultWrapper.buildFailure("获取工单组盘容器信息失败: " + e.getMessage());
        }
    }
//    /**
//     * 提供容器物料列表 用于工装与订单建立关系
//     */
//    @PostMapping("/find-container-material")
//    public ResultWrapper<List<String>> findContainerMaterialInfo(@RequestBody PutAwayCombineDto putAwayCombineDto) {
//
//        return ResultWrapper.buildSuccess();
//    }
    /**
     * 订单+工装容器 建立bind关系
     */
    @PostMapping("/tooling-order-container-bind")
    public ResultWrapper<Void> toolingOrderContainerBind(@RequestBody ToolingOrderContainerBindDto dto) {
        try {
            inboundService.toolingOrderContainerBind(dto);
            return ResultWrapper.buildSuccess(null);
        } catch (Exception e) {
            return ResultWrapper.buildFailure("获取工单组盘容器信息失败: " + e.getMessage());
        }
    }
    /**
     * 新到工装入库容器离开
     */
    @PostMapping("/container-leave-station")
    public ResultWrapper<Void> containerLeaveStation(@RequestBody ContainerLeaveDto dto) {
        try {

            String workstationCode = dto.getWorkstationCode();
            String containerCode = dto.getContainerCode();

            String houseCode = WmsConfig.DefaultHouseCode;
            TaskApplyParam applyParam = new TaskApplyParam.Builder()
                    .setSystemCode("01")
                    .setHouseCode(houseCode)
                    .setDeviceCode(workstationCode)
                    .setContainerCode(containerCode)
                    .setContainerShape(null)
                    .setParameters(null)
                    .build();
            MessageResult result = transJobAppService.applyTransJob(applyParam);
            if (!result.getSuccess()) {
                return ResultWrapper.buildFailure("入库申请失败: " + result.getMessage());
            }
            return ResultWrapper.buildSuccess(null);
        } catch (Exception e) {
            return ResultWrapper.buildFailure("入库申请失败: " + e.getMessage());
        }
    }
    /**
     * 新到工装入库容器校验
     */
    @PostMapping("/container-check")
    public ResultWrapper<Void> containerCheck(@RequestBody ContainerLeaveDto dto) {
        try {
            return ResultWrapper.buildSuccess(null);
        } catch (Exception e) {
            return ResultWrapper.buildFailure("新到工装入库箱校验: " + e.getMessage());
        }
    }
}