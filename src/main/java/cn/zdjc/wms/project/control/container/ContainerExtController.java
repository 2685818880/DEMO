package cn.zdjc.wms.project.control.container;

import cn.zdjc.warehouse.WmsConfig;
import cn.zdjc.warehouse.container.application.service.ContainerAppService;
import cn.zdjc.warehouse.container.application.service.ContainerTypeAppService;
import cn.zdjc.warehouse.container.domain.dto.ContainerTypeDto;
import cn.zdjc.warehouse.container.domain.query.ContainerTypeExQuery;
import cn.zdjc.warehouse.definition.application.query.DeviceStatusExQuery;
import cn.zdjc.warehouse.definition.domain.dto.DeviceStatusDto;
import cn.zdjc.warehouse.definition.domain.service.DeviceStatusService;
import cn.zdjc.warehouse.definition.infrastructure.enums.DeviceType;
import cn.zdjc.wms.api.dto.TaskApplyParam;
import cn.zdjc.wms.api.service.TransJobAppService;
import cn.zdjc.wms.common.utils.StringUtils;
import cn.zdjc.wms.definition.application.service.PalletizeAppService;
import cn.zdjc.wms.definition.application.service.PalletizeItemAppService;
import cn.zdjc.wms.definition.domain.service.CarryService;
import cn.zdjc.wms.definition.domain.service.EmptyService;
import cn.zdjc.wms.definition.domain.service.PalletizeFormService;
import cn.zdjc.wms.definition.infrastructure.PrimaryGenerator;
import cn.zdjc.wms.project.common.constant.ProjectConstant;
import cn.zdjc.wms.project.domain.dto.palletize.CallContainerExtQuery;
import cn.zdjc.wms.project.domain.dto.palletize.RegisterEmptyTrayExtQuery;
import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import com.foeris.y.common.result.MessageResult;
import com.foeris.y.common.string.StringUtl;
import com.foreris.eris.common.exception.BusinessException;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 容器管理控制器
 *
 * <p>提供容器相关的业务接口，包括：
 * <ul>
 *   <li>空托盘注册</li>
 *   <li>托盘呼叫分配</li>
 *   <li>出库口设备查询</li>
 *   <li>容器类型查询</li>
 * </ul>
 * </p>
 *
 * @author WMS Team
 * @version 1.0
 * @since 2026-02-04
 */
@RestController
@RequestMapping(ProjectConstant.PREFIX_CONTEXT_PATH + "/container")
public class ContainerExtController {

    private static final Logger log = LoggerFactory.getLogger(ContainerExtController.class);

    /** 托盘类型分隔符 */
    private static final String CONTAINER_TYPE_SEPARATOR = ",";

    /** 默认托盘前缀 */
    private static final String DEFAULT_PALLET_PREFIX = "T";

    @Resource
    private PalletizeFormService palletizeService;

    @Resource
    private PalletizeAppService palletizeAppService;

    @Resource
    private EmptyService emptyService;

    @Resource
    private ContainerAppService containerAppService;

    @Resource
    private PalletizeItemAppService palletizeItemAppService;

    @Resource
    private CarryService carryService;

    @Resource
    private DeviceStatusService deviceStatusService;

    @Resource
    private ContainerTypeAppService containerTypeAppService;

    @Resource
    private TransJobAppService transJobAppService;

    /**
     * 空托盘注册接口
     *
     * <p>用于在系统中注册新的空托盘，支持自动生成托盘编号或使用指定编号。</p>
     *
     * <h3>业务规则：</h3>
     * <ul>
     *   <li>如果未提供托盘编号，系统将自动生成唯一编号（格式：T + 时间戳 + 序列号）</li>
     *   <li>托盘编号必须唯一，重复注册将失败</li>
     *   <li>库区编码为必填项，用于确定托盘归属</li>
     *   <li>空托数量默认为1，可根据实际需求调整</li>
     * </ul>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>新采购托盘入库</li>
     *   <li>托盘维修后重新投入使用</li>
     *   <li>托盘盘点补录</li>
     * </ul>
     *
     * @param query 空托盘注册查询参数
     *              <ul>
     *                <li>{@code houseCode} - 库区编码（必填）</li>
     *                <li>{@code containerCode} - 托盘编号（选填，为空时自动生成）</li>
     *                <li>{@code emptyQty} - 空托数量（默认1）</li>
     *                <li>{@code fromPos} - 来源位置（选填）</li>
     *              </ul>
     * @return 返回注册后的托盘编号
     * @throws BusinessException 业务异常（如参数校验失败、托盘已存在等）
     */
    @PostMapping(value = "/register")
    @ResponseBody
    public ResultWrapper<String> registerEmptyTray(@Valid @RequestBody RegisterEmptyTrayExtQuery query) {
        log.info("【空托盘注册】请求开始 | houseCode={}, containerCode={}, emptyQty={}, fromPos={}",
                query.getHouseCode(), query.getContainerCode(), query.getEmptyQty(), query.getFromPos());

        try {
            // 参数校验
            validateRegisterParams(query);

            // 自动生成托盘编号（如未提供）
            String containerCode = generateContainerCodeIfNeeded(query);
            query.setContainerCode(containerCode);

            // 执行注册
            emptyService.registerEmptyTray(
                    query.getHouseCode(),
                    containerCode,
                    query.getEmptyQty(),
                    query.getFromPos()
            );

            log.info("【空托盘注册】成功 | containerCode={}", containerCode);
            return ResultWrapper.buildSuccess(containerCode);

        } catch (BusinessException e) {
            log.error("【空托盘注册】业务异常 | containerCode={}, message={}",
                    query.getContainerCode(), e.getMessage(), e);
            return ResultWrapper.buildFailure("容器注册失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("【空托盘注册】系统异常 | containerCode={}", query.getContainerCode(), e);
            return ResultWrapper.buildFailure("容器注册失败: " + e.getMessage());
        }
    }

    /**
     * 呼叫托盘接口
     *
     * <p>根据指定条件从系统中分配可用的空托盘，用于出库、拣选等业务场景。</p>
     *
     * <h3>分配策略：</h3>
     * <ul>
     *   <li>优先分配指定库区的空托盘</li>
     *   <li>支持按托盘类型筛选（可多选）</li>
     *   <li>按托盘数量进行批量分配</li>
     *   <li>分配后托盘状态自动更新为"已占用"</li>
     * </ul>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>出库作业前准备托盘</li>
     *   <li>拣选作业托盘分配</li>
     *   <li>生产线物料配送托盘准备</li>
     * </ul>
     *
     * @param query 托盘呼叫查询参数
     *              <ul>
     *                <li>{@code houseCode} - 库区编码（必填）</li>
     *                <li>{@code toPos} - 目标位置/站点（必填）</li>
     *                <li>{@code count} - 需要的托盘数量（必填）</li>
     *                <li>{@code containerType} - 托盘类型（必填，多个类型用逗号分隔）</li>
     *              </ul>
     * @return 返回分配成功的托盘编号列表
     * @throws BusinessException 业务异常（如无可分配托盘、参数错误等）
     */
    @PostMapping(value = "/call_container")
    @ResponseBody
    public ResultWrapper<List<String>> callContainer(@Valid @RequestBody CallContainerExtQuery query) {
        log.info("【托盘呼叫】请求开始 | houseCode={}, toPos={}, count={}, containerType={}",
                query.getHouseCode(), query.getToPos(), query.getCount(), query.getContainerType());
        query.setHouseCode(WmsConfig.DefaultHouseCode);

        try {
            // 校验呼叫参数
            validateCallContainerParams(query);

            // 将容器类型字符串转换为列表
            List<String> containerTypes = parseContainerTypes(query.getContainerType());

            // 执行托盘分配
            List<String> allocatedContainers = emptyService.applyEmptyTray(
                    query.getHouseCode(),
                    query.getToPos(),
                    query.getCount(),
                    containerTypes
            );
            String houseCode=WmsConfig.DefaultHouseCode;
            String remark = query.getRemark();
            String orderContainer = query.getOrderContainer();
            if (!StringUtils.isEmpty(remark)&&"changeBox".equals(remark)&&
                    !StringUtils.isEmpty(orderContainer)) {
                TaskApplyParam applyParam = new TaskApplyParam.Builder()
                        .setSystemCode("01")
                        .setHouseCode(houseCode)
                        .setDeviceCode(query.getToPos())
                        .setContainerCode(orderContainer)
                        .setContainerShape(null)
                        .setParameters(null)
                        .build();
                MessageResult result = transJobAppService.applyTransJob(applyParam);
                if (!result.getSuccess()) {
                    return ResultWrapper.buildFailure("入库申请失败: " + result.getMessage());
                }
            }

            // 检查分配结果
            if (CollectionUtils.isEmpty(allocatedContainers)) {
                log.warn("【托盘呼叫】分配失败 - 无可分配空托 | houseCode={}, toPos={}, count={}, containerType={}",
                        query.getHouseCode(), query.getToPos(), query.getCount(), query.getContainerType());
                return ResultWrapper.buildFailure("无可分配空托");
            }

            log.info("【托盘呼叫】成功 | 分配数量={}, 托盘列表={}",
                    allocatedContainers.size(), allocatedContainers);
            return ResultWrapper.buildSuccess(allocatedContainers);

        } catch (BusinessException e) {
            log.error("【托盘呼叫】业务异常 | houseCode={}, message={}",
                    query.getHouseCode(), e.getMessage(), e);
            return ResultWrapper.buildFailure("托盘分配失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("【托盘呼叫】系统异常 | houseCode={}", query.getHouseCode(), e);
            return ResultWrapper.buildFailure("托盘分配失败: " + e.getMessage());
        }
    }

    /**
     * 获取出库口设备列表
     *
     * <p>查询系统中所有可用的出库口设备，用于托盘出库作业的设备选择。</p>
     *
     * <h3>查询条件：</h3>
     * <ul>
     *   <li>设备类型为"出库口"（DeviceType.Exit）</li>
     *   <li>可根据库区编码进行过滤（可选）</li>
     *   <li>仅返回状态正常的设备</li>
     * </ul>
     *
     * <h3>返回数据：</h3>
     * <ul>
     *   <li>设备编码列表（按设备编码排序）</li>
     *   <li>空列表表示无可用出库设备</li>
     * </ul>
     *
     * @param query 出库设备查询参数（可选：houseCode - 库区编码）
     * @return 返回出库口设备编码列表
     */
    @PostMapping("/exitDevice")
    public ResultWrapper<List<String>> getExitDeviceList(@RequestBody(required = false) DeviceStatusExQuery query) {
        log.debug("【出库设备查询】请求开始 | query={}", query);

        try {
            // 初始化查询条件
            if (query == null) {
                query = new DeviceStatusExQuery();
            }

            // 设置设备类型为出库口
            query.setDevice_type(DeviceType.Exit);

            // 执行查询
            List<DeviceStatusDto> deviceList = deviceStatusService.queryList(query);

            // 转换为设备编码列表
            List<String> deviceCodes = convertToDeviceCodes(deviceList);

            log.info("【出库设备查询】成功 | 设备数量={}", deviceCodes.size());
            return ResultWrapper.buildSuccess(deviceCodes);

        } catch (Exception e) {
            log.error("【出库设备查询】异常 | error={}", e.getMessage(), e);
            return ResultWrapper.buildFailure("查询出库设备失败: " + e.getMessage());
        }
    }

    /**
     * 获取容器类型列表
     *
     * <p>查询系统中所有已配置的容器（托盘）类型标准，用于托盘分类管理。</p>
     *
     * <h3>数据来源：</h3>
     * <ul>
     *   <li>容器标准配置表</li>
     *   <li>包含所有启用和未启用的容器类型</li>
     * </ul>
     *
     * <h3>返回数据：</h3>
     * <ul>
     *   <li>容器类型编码列表（唯一标识）</li>
     *   <li>空列表表示无配置的容器类型</li>
     * </ul>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>托盘注册时选择类型</li>
     *   <li>托盘查询时的类型筛选</li>
     *   <li>报表统计按类型分组</li>
     * </ul>
     *
     * @param query 容器类型查询参数（预留，当前未使用）
     * @return 返回容器类型编码列表
     */
    @PostMapping("/containerType")
    public ResultWrapper<List<String>> getContainerTypeList(@RequestBody(required = false) ContainerTypeExQuery query) {
        log.debug("【容器类型查询】请求开始");

        try {
            // 执行查询（忽略传入的查询条件，查询所有）
            List<ContainerTypeDto> standardList = containerTypeAppService.grid_list(new ContainerTypeExQuery());

            // 转换为类型编码列表
            List<String> standardCodes = convertToStandardCodes(standardList);

            log.info("【容器类型查询】成功 | 类型数量={}", standardCodes.size());
            return ResultWrapper.buildSuccess(standardCodes);

        } catch (Exception e) {
            log.error("【容器类型查询】异常 | error={}", e.getMessage(), e);
            return ResultWrapper.buildFailure("查询容器类型失败: " + e.getMessage());
        }
    }

    // ========== 私有辅助方法 ==========

    /**
     * 校验空托盘注册参数
     */
    private void validateRegisterParams(RegisterEmptyTrayExtQuery query) {
        if (StringUtl.isEmpty(query.getHouseCode())) {
            throw new BusinessException("库区编码不能为空");
        }

        if (query.getEmptyQty() == null || query.getEmptyQty() <= 0) {
            throw new BusinessException("空托数量必须大于0");
        }

        log.debug("【参数校验】注册参数校验通过");
    }

    /**
     * 根据需要生成托盘编号
     *
     * @param query 注册查询参数
     * @return 托盘编号（如未提供则自动生成）
     */
    private String generateContainerCodeIfNeeded(RegisterEmptyTrayExtQuery query) {
        if (StringUtl.isEmpty(query.getContainerCode())) {
            String generatedCode = PrimaryGenerator.getInstance().nextFormNo(DEFAULT_PALLET_PREFIX);
            log.debug("【托盘编号】自动生成托盘编号: {}", generatedCode);
            return generatedCode;
        }
        return query.getContainerCode();
    }

    /**
     * 校验托盘呼叫参数
     */
    private void validateCallContainerParams(CallContainerExtQuery query) {
        if (StringUtl.isEmpty(query.getHouseCode())) {
            throw new BusinessException("库区编码不能为空");
        }

        if (StringUtl.isEmpty(query.getToPos())) {
            throw new BusinessException("目标位置不能为空");
        }

        if ( query.getCount() <= 0) {
            throw new BusinessException("托盘数量必须大于0");
        }

        if (StringUtl.isEmpty(query.getContainerType())) {
            throw new BusinessException("托盘类型不能为空");
        }

        log.debug("【参数校验】呼叫参数校验通过");
    }

    /**
     * 解析容器类型字符串为列表
     *
     * @param containerType 容器类型字符串（多个类型用逗号分隔）
     * @return 容器类型列表
     */
    private List<String> parseContainerTypes(String containerType) {
        if (StringUtl.isEmpty(containerType)) {
            return Collections.emptyList();
        }

        List<String> types = Arrays.stream(containerType.split(CONTAINER_TYPE_SEPARATOR))
                .map(String::trim)
                .filter(type -> !type.isEmpty())
                .collect(Collectors.toList());

        log.debug("【类型解析】解析容器类型: {} -> {}", containerType, types);
        return types;
    }

    /**
     * 将设备列表转换为设备编码列表
     *
     * @param deviceList 设备DTO列表
     * @return 设备编码列表
     */
    private List<String> convertToDeviceCodes(List<DeviceStatusDto> deviceList) {
        if (CollectionUtils.isEmpty(deviceList)) {
            log.debug("【数据转换】设备列表为空，返回空列表");
            return Collections.emptyList();
        }

        List<String> deviceCodes = deviceList.stream()
                .map(DeviceStatusDto::getDevice_code)
                .filter(Objects::nonNull)
                .sorted() // 按设备编码排序
                .collect(Collectors.toList());

        log.debug("【数据转换】转换设备数量: {} -> {}", deviceList.size(), deviceCodes.size());
        return deviceCodes;
    }

    /**
     * 将容器标准列表转换为标准编码列表
     *
     * @param standardList 容器标准DTO列表
     * @return 标准编码列表
     */
    private List<String> convertToStandardCodes(List<ContainerTypeDto> standardList) {
        if (CollectionUtils.isEmpty(standardList)) {
            log.debug("【数据转换】容器标准列表为空，返回空列表");
            return Collections.emptyList();
        }

        List<String> standardCodes = standardList.stream()
                .map(ContainerTypeDto::getContainer_type_code)
                .filter(Objects::nonNull)
                .distinct() // 去重
                .sorted()   // 排序
                .collect(Collectors.toList());

        log.debug("【数据转换】转换标准数量: {} -> {}", standardList.size(), standardCodes.size());
        return standardCodes;
    }
}