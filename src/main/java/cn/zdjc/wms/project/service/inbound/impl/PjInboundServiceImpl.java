package cn.zdjc.wms.project.service.inbound.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.zdjc.warehouse.WmsConfig;
import cn.zdjc.warehouse.container.application.service.ContainerAppService;
import cn.zdjc.warehouse.container.domain.dto.ContainerDto;
import cn.zdjc.warehouse.definition.infrastructure.enums.LocationType;
import cn.zdjc.warehouse.inventory.application.service.ContainerMaterialAppService;
import cn.zdjc.warehouse.inventory.application.service.StorageLocationInventoryAppService;
import cn.zdjc.warehouse.inventory.domain.dto.ContainerMaterialDto;
import cn.zdjc.warehouse.inventory.domain.dto.StorageLocationInventoryDto;
import cn.zdjc.warehouse.inventory.domain.dto.StorageMaterialDto;
import cn.zdjc.warehouse.inventory.domain.service.StorageMaterialService;
import cn.zdjc.warehouse.inventory.infrastructure.enums.InventoryStatus;
import cn.zdjc.warehouse.inventory.infrastructure.enums.QualityStatus;
import cn.zdjc.warehouse.material.application.service.SkuAppService;
import cn.zdjc.warehouse.material.domain.dto.SkuDto;
import cn.zdjc.warehouse.strategy.application.allotPolicy.ExQueryBean;
import cn.zdjc.wms.common.application.dto.UnitGroupDto;
import cn.zdjc.wms.common.application.service.IUnitAppService;
import cn.zdjc.wms.common.application.service.IUnitGroupAppService;
import cn.zdjc.wms.core.protocol.mes.MesProtocolFactory;
import cn.zdjc.wms.core.protocol.mes.dto.ReceiveMaterialDto;
import cn.zdjc.wms.definition.application.dto.StartPalletizeDto;
import cn.zdjc.wms.definition.application.query.AsnItemExQuery;
import cn.zdjc.wms.definition.application.service.PalletizeAppService;
import cn.zdjc.wms.definition.application.service.cmds.PalletizeFormQueryCmd;
import cn.zdjc.wms.definition.domain.dto.AsnDetailDto;
import cn.zdjc.wms.definition.domain.dto.AsnItemDto;
import cn.zdjc.wms.definition.domain.dto.PalletizeFormDto;
import cn.zdjc.wms.definition.domain.dto.PalletizeItemDto;
import cn.zdjc.wms.definition.domain.service.AsnItemService;
import cn.zdjc.wms.definition.domain.service.PalletizeFormService;
import cn.zdjc.wms.definition.domain.service.PalletizeItemService;
import cn.zdjc.wms.definition.infrastructure.PrimaryGenerator;
import cn.zdjc.wms.definition.infrastructure.enums.AsnStatus;
import cn.zdjc.wms.definition.infrastructure.enums.ManualType;
import cn.zdjc.wms.definition.infrastructure.enums.PalletizeFormStatus;
import cn.zdjc.wms.definition.infrastructure.enums.PalletizeType;
import cn.zdjc.wms.inbound.application.service.AsnDetailAppService;
import cn.zdjc.wms.project.common.utils.SqlUtil;
import cn.zdjc.wms.project.domain.dto.asn.AsnItemExtDto;
import cn.zdjc.wms.project.domain.dto.inbound.*;
import cn.zdjc.wms.project.domain.dto.ws.WorkstationExtDto;
import cn.zdjc.wms.project.domain.entity.palletize.PalletizeItemExtEntity;
import cn.zdjc.wms.project.domain.entity.ws.WorkstationExtEntity;
import cn.zdjc.wms.project.domain.query.asn.AsnItemExtExQuery;
import cn.zdjc.wms.project.domain.query.palletize.PalletizeItemExtQuery;
import cn.zdjc.wms.project.domain.query.ws.WorkstationExtQuery;
import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import cn.zdjc.wms.project.service.asn.AsnItemExtService;
import cn.zdjc.wms.project.service.inbound.PjInboundService;
import cn.zdjc.wms.project.service.palletize.PalletizeItemExtService;
import com.foeris.y.common.jdbc.SqlUtl;
import com.foeris.y.common.result.PageResult;
import com.foeris.y.common.string.StringUtl;
import com.foeris.y.fairy.jdbc.connection.DatabaseExecuter;
import com.foreris.eris.common.exception.BusinessException;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PjInboundServiceImpl implements PjInboundService {

    @Resource
    private AsnItemService asnItemService;

    @Resource
    private AsnItemExtService asnItemExtService;

    @Resource
    private PalletizeAppService palletizeAppService;

    @Resource
    private SkuAppService skuAppService;

    @Resource
    private PalletizeItemExtService palletizeItemExtService;

    @Resource
    private IUnitGroupAppService iUnitGroupAppService;

    @Resource
    private PalletizeFormService palletizeFormService;

    @Resource
    private StorageLocationInventoryAppService storageLocationInventoryAppService;

    @Resource
    private ContainerMaterialAppService containerMaterialAppService;

    @Resource
    private ContainerAppService containerAppService;

    @Resource
    private MesProtocolFactory mesProtocolFactory;

    @Resource
    private StorageMaterialService storageMaterialService;

    @Resource
    private PalletizeItemService palletizeItemService;

    @Autowired
    private AsnDetailAppService asnDetailAppService;

    // ========== 工具方法：安全 trim（用于 toMap key 生成等场景）==========
    private static String safeTrim(String str) {
        return Optional.ofNullable(str)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElse(null);
    }

    @Override
    public List<String> orderNoList(String skuCode) {
        AsnItemExtExQuery query = new AsnItemExtExQuery();
        query.setSkuCode(skuCode);

        List<String> statusList = Arrays.asList(String.valueOf(AsnStatus.Created), String.valueOf(AsnStatus.Executing));
        query.setAsnStatusList(statusList);
        List<AsnItemExtDto> itemDtoList = asnItemExtService.queryListWithAsnHeader(query);
        if (CollUtil.isEmpty(itemDtoList)) {
            throw new BusinessException("收货明细查询异常: 未找到物料[{}]的收料明细记录，请检查填写的信息是否正确！", skuCode);
        }

        return itemDtoList.stream()
                .map(AsnItemExtDto::getBusinessFormNo)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderExtDto> orderNoInfoList(AsnItemExtExQuery query) {
        if (query == null) {
            log.warn("查询参数 query 为空");
            query = new AsnItemExtExQuery();
        }

        List<AsnItemExtDto> itemDtoList = asnItemExtService.queryListWithAsnHeader(query);
        if (itemDtoList == null || itemDtoList.isEmpty()) {
            return Collections.emptyList();
        }

        return itemDtoList.stream()
                .filter(Objects::nonNull)
                .map(AsnItemExtDto::getBusinessFormNo)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .distinct()
                .map(orderNo -> {
                    OrderExtDto dto = new OrderExtDto();
                    dto.setBusinessOrderNo(orderNo);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<OrderExtDto> orderNoPageInfo(AsnItemExtExQuery query) {
        log.info("查询 ASN 明细 - 条件: {}", query);

        if (query == null) {
            log.warn("查询参数 query 为空");
            query = new AsnItemExtExQuery();
        }

        PageResult<AsnItemExtDto> asnItemExtDtoPageResult = asnItemExtService.queryAsnItemAndAsnPagePageList(query);
        if (asnItemExtDtoPageResult == null || asnItemExtDtoPageResult.getRows() == null) {
            log.warn("查询结果为空");
            return new PageResult<>(Collections.emptyList(), query.getPage(), query.getRow(), 0);
        }

        List<AsnItemExtDto> itemDtoList = asnItemExtDtoPageResult.getRows();
        int total = itemDtoList.size();

        List<OrderExtDto> collect = itemDtoList.stream()
                .filter(Objects::nonNull)
                .map(AsnItemExtDto::getBusinessFormNo)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .distinct()
                .map(orderNo -> {
                    OrderExtDto dto = new OrderExtDto();
                    dto.setBusinessOrderNo(orderNo);
                    return dto;
                })
                .collect(Collectors.toList());

        return new PageResult<>(collect, query.getPage(), query.getRow(), total);
    }

    @Transactional
    @Override
    public void putAway(PutAwayCombineDto putAwayCombineDto) {
        log.info("【上架组盘】开始处理单条收货上架请求：业务单号={}, SKU 编码={}, 容器编码={}, 上架数量={}",
                putAwayCombineDto.getBusinessOrderNo(),
                putAwayCombineDto.getSkuCode(),
                putAwayCombineDto.getContainerCode(),
                putAwayCombineDto.getQty());

        Assert.hasText(putAwayCombineDto.getBusinessOrderNo(), "业务单号不能为空");
        Assert.hasText(putAwayCombineDto.getBusinessItemNo(), "业务行号不能为空");
        Assert.hasText(putAwayCombineDto.getSkuCode(), "SKU 编码不能为空");
        Assert.hasText(putAwayCombineDto.getContainerCode(), "容器编码不能为空");
        Assert.notNull(putAwayCombineDto.getQty(), "上架数量不能为空");
        Assert.isTrue(putAwayCombineDto.getQty().compareTo(BigDecimal.ZERO) > 0, "上架数量必须大于 0");

        AsnItemExQuery queryParam = new AsnItemExQuery();
        queryParam.setBusiness_form_no(putAwayCombineDto.getBusinessOrderNo());
        queryParam.setBusiness_item_no(putAwayCombineDto.getBusinessItemNo());
        queryParam.setSku_code(putAwayCombineDto.getSkuCode());

        List<AsnItemDto> itemDtoList = asnItemService.queryList(queryParam);
        if (CollUtil.isEmpty(itemDtoList)) {
            throw new BusinessException("收货明细查询异常：单据 [{}] 中未找到物料 [{}] 的收料明细记录",
                    putAwayCombineDto.getBusinessOrderNo(), putAwayCombineDto.getSkuCode());
        }
        if (itemDtoList.size() > 1) {
            throw new BusinessException("收货明细查询异常：单据 [{}] 中物料 [{}] 存在多条收料明细，请检查批次或行号",
                    putAwayCombineDto.getBusinessOrderNo(), putAwayCombineDto.getSkuCode());
        }

        AsnItemDto asnItemDto = itemDtoList.get(0);
        String asnNo = asnItemDto.getAsn_no();
        String itemNo = asnItemDto.getItem_no();

        Double primaryQtyDouble = asnItemDto.getPrimary_qty();
        Double confirmQtyDouble = asnItemDto.getConfirm_qty();
        if (primaryQtyDouble == null || confirmQtyDouble == null) {
            log.error("【上架组盘】ASN 明细数量数据异常，asnNo={}, itemNo={}", asnNo, itemNo);
            throw new BusinessException("收货明细数量数据异常，请联系管理员");
        }

        BigDecimal primaryQty = new BigDecimal(primaryQtyDouble.toString());
        BigDecimal confirmQty = new BigDecimal(confirmQtyDouble.toString());
        BigDecimal surplusQty = primaryQty.subtract(confirmQty);

        if (surplusQty.compareTo(putAwayCombineDto.getQty()) < 0) {
            throw new BusinessException("上架数量 [{}] 超过剩余可上架数量 [{}]，业务单号：{}",
                    putAwayCombineDto.getQty(), surplusQty, putAwayCombineDto.getBusinessOrderNo());
        }

        StartPalletizeDto dto = new StartPalletizeDto();
        dto.setHouse_code(WmsConfig.DefaultHouseCode);
        dto.setContainer_code(putAwayCombineDto.getContainerCode());
        dto.setSku_code(putAwayCombineDto.getSkuCode());
        dto.setQty(putAwayCombineDto.getQty().doubleValue());
        dto.setAsn_no(asnNo);
        dto.setItemNo(itemNo);

        log.info("【上架组盘】调用组盘服务：ASN 单号={}, SKU={}, 数量={}, 容器={}",
                asnNo, dto.getSku_code(), dto.getQty(), dto.getContainer_code());

        palletizeAppService.receiptGroupTrayBySku(dto);

        log.info("【上架组盘】操作成功完成：业务单号={}, 容器编码={}",
                putAwayCombineDto.getBusinessOrderNo(),
                putAwayCombineDto.getContainerCode());
    }

    @Transactional
    @Override
    public void batchPutAway(BatchPutAwayCombineDto batchPutAwayCombineDto) {
        String houseCode = WmsConfig.DefaultHouseCode;
        String businessOrderNo = batchPutAwayCombineDto.getBusinessOrderNo();
        String containerCode = batchPutAwayCombineDto.getContainerCode();

        Assert.hasText(containerCode, "容器编码不能为空");
        if (CollUtil.isEmpty(batchPutAwayCombineDto.getItemList())) {
            throw new BusinessException("上架物料列表不能为空");
        }
        String deviceCode = batchPutAwayCombineDto.getDeviceCode();
        StartPalletizeDto startDto = new StartPalletizeDto();
        startDto.setHouse_code(houseCode);
        startDto.setContainer_code(containerCode);
        startDto.setDeviceCode(deviceCode);
        startDto.setDevice_code(deviceCode);
        List<StorageMaterialDto> itemList = new ArrayList<>();

        for (PjInventoryReceiptItemDto itemDto : batchPutAwayCombineDto.getItemList()) {
            Assert.hasText(itemDto.getSkuCode(), "SKU 编码不能为空");
            Assert.notNull(itemDto.getQty(), "数量不能为空");
            Assert.isTrue(itemDto.getQty().compareTo(BigDecimal.ZERO) > 0, "数量必须大于 0");

            String skuCode = itemDto.getSkuCode();
            String businessOrderNoRow = itemDto.getBusinessOrderNo();
            if (StringUtils.isBlank(businessOrderNoRow)) {
                businessOrderNoRow = businessOrderNo;
            }

            AsnItemExtExQuery query = new AsnItemExtExQuery();
            query.setBusinessOrderNo(businessOrderNoRow);
            query.setSkuCode(skuCode);

            List<AsnItemExtDto> asnItems = asnItemExtService.queryListWithAsnHeader(query);

            if (CollUtil.isEmpty(asnItems)) {
                throw new BusinessException("未找到业务单号 [{}] 行号 [{}] 的 ASN 收货明细", businessOrderNo, itemDto.getBusinessItemNo());
            }
            if (asnItems.size() > 1) {
                throw new BusinessException("业务单号 [{}] 行号 [{}] 对应多条 ASN 明细，请检查", businessOrderNo, itemDto.getBusinessItemNo());
            }
            AsnItemExtDto asnItem = asnItems.get(0);
            String businessItemNo = asnItem.getBusinessItemNo();
            SkuDto skuDto = skuAppService.getSkuByCode(skuCode);
            if (Objects.isNull(skuDto)) {
                throw new BusinessException("SKU[{}] 不存在", itemDto.getSkuCode());
            }

            UnitGroupDto unitGroupDto = iUnitGroupAppService.queryUnitGroupById(skuDto.getUnit_plan_id());
            if (unitGroupDto == null) {
                throw new BusinessException("SKU[{}] 单位方案不存在", skuCode);
            }

            StorageMaterialDto materialDto = new StorageMaterialDto();
            materialDto.setSku_id(skuDto.getId());
            materialDto.setHouse_code(houseCode);
            materialDto.setCategory_id(skuDto.getCategory_id());
            materialDto.setCategory_code(skuDto.getCategory_code());
            materialDto.setSku_code(skuCode);
            materialDto.setSku_name(skuDto.getSku_name());
            materialDto.setBatch_no(itemDto.getBatchNo());
            materialDto.setSerial_no(PrimaryGenerator.getInstance().nextFormNo("SN"));
            materialDto.setInventory_status(InventoryStatus.A);
            materialDto.setQuality_status(QualityStatus.Q);
            materialDto.setPrimary_qty(itemDto.getQty().doubleValue());
            materialDto.setPrimary_unit(unitGroupDto.getPrimaryUnitName());
            materialDto.setAuxiliary_qty(itemDto.getQty().doubleValue());
            materialDto.setAuxiliary_unit(unitGroupDto.getBaseUnitName());
            materialDto.setAvailable_qty(itemDto.getQty().doubleValue());
            materialDto.setBusiness_form_no(businessOrderNo);
            materialDto.setBusiness_item_no(businessItemNo);
            itemList.add(materialDto);

            startDto.setId(UUID.fromString(asnItem.getId()));

            startDto.setItem_no(asnItem.getItemNo());
            startDto.setAsn_no(asnItem.getAsnNo());
            startDto.setItemNo(asnItem.getItemNo());
            startDto.setItemList(itemList);
            palletizeAppService.groupTray(startDto);
        }

        log.info("【批量上架组盘】操作完成：容器编码={}, 处理物料项数={}", containerCode, itemList.size());
    }

    @Transactional
    @Override
    public void toolingBatchPutAway(BatchPutAwayCombineDto batchPutAwayCombineDto) {
        String houseCode = WmsConfig.DefaultHouseCode;
        String containerCode = batchPutAwayCombineDto.getContainerCode();
        Integer sourceType = batchPutAwayCombineDto.getSourceType();

        Assert.hasText(containerCode, "容器编码不能为空");
        if (CollUtil.isEmpty(batchPutAwayCombineDto.getItemList())) {
            throw new BusinessException("上架物料列表不能为空");
        }
        String deviceCode = batchPutAwayCombineDto.getDeviceCode();
        StartPalletizeDto startDto = new StartPalletizeDto();
        startDto.setHouse_code(houseCode);
        startDto.setContainer_code(containerCode);
        startDto.setDeviceCode(deviceCode);
        startDto.setDevice_code(deviceCode);
        startDto.setPalletize_type(PalletizeType.Common);

        List<StorageMaterialDto> itemList = new ArrayList<>();

        for (PjInventoryReceiptItemDto itemDto : batchPutAwayCombineDto.getItemList()) {
            Assert.hasText(itemDto.getSkuCode(), "SKU 编码不能为空");
            Assert.notNull(itemDto.getQty(), "数量不能为空");
            Assert.isTrue(itemDto.getQty().compareTo(BigDecimal.ZERO) > 0, "数量必须大于 0");

            String skuCode = itemDto.getSkuCode();
            String remark = itemDto.getRemark();

            SkuDto skuDto = skuAppService.getSkuByCode(skuCode);
            if (Objects.isNull(skuDto)) {
                throw new BusinessException("SKU[{}] 不存在", itemDto.getSkuCode());
            }

            UnitGroupDto unitGroupDto = iUnitGroupAppService.queryUnitGroupById(skuDto.getUnit_plan_id());
            if (unitGroupDto == null) {
                throw new BusinessException("SKU[{}] 单位方案不存在", skuCode);
            }

            StorageMaterialDto materialDto = new StorageMaterialDto();
            materialDto.setSku_id(skuDto.getId());
            materialDto.setHouse_code(houseCode);
            materialDto.setCategory_id(skuDto.getCategory_id());
            materialDto.setCategory_code(skuDto.getCategory_code());
            materialDto.setSku_code(skuCode);
            materialDto.setSku_name(skuDto.getSku_name());
            materialDto.setBatch_no(itemDto.getBatchNo());
            materialDto.setSerial_no(PrimaryGenerator.getInstance().nextFormNo("SN"));
            materialDto.setInventory_status(InventoryStatus.A);
            materialDto.setQuality_status(QualityStatus.Q);
            materialDto.setPrimary_qty(itemDto.getQty().doubleValue());
            materialDto.setPrimary_unit(unitGroupDto.getPrimaryUnitName());
            materialDto.setAuxiliary_qty(itemDto.getQty().doubleValue());
            materialDto.setAuxiliary_unit(unitGroupDto.getBaseUnitName());
            materialDto.setAvailable_qty(itemDto.getQty().doubleValue());
            materialDto.setBusiness_form_no(null);
            materialDto.setBusiness_item_no(null);
            materialDto.setRemark(remark);
            itemList.add(materialDto);

            startDto.setItem_no(null);
            startDto.setAsn_no(null);
            startDto.setItemNo(null);
            startDto.setItemList(itemList);
            palletizeAppService.groupTray(startDto);
        }
        log.info("【批量上架组盘】操作完成：容器编码={}, 处理物料项数={}", containerCode, itemList.size());
    }

    @Transactional
    @Override
    public ResultWrapper<Void> combineUnbind(BatchPutAwayCombineDto batchPutAwayCombineDto) {
        if (batchPutAwayCombineDto == null) {
            log.warn("【组盘解绑】收到空参数请求");
            return ResultWrapper.buildFailure("【组盘解绑】收到空参数请求!");
        }

        String containerCode = batchPutAwayCombineDto.getContainerCode();
        if (StrUtil.isBlank(containerCode)) {
            return ResultWrapper.buildFailure("【组盘解绑】容器编码为空，无法执行解绑操作");
        }

        log.info("【组盘解绑】开始处理容器解绑请求：容器编码={}", containerCode);

        PalletizeFormQueryCmd queryCmd = new PalletizeFormQueryCmd();
        queryCmd.setContainerCode(containerCode);
        queryCmd.setFormStatusList(Arrays.asList(PalletizeFormStatus.Created, PalletizeFormStatus.Executing));

        List<PalletizeFormDto> formDtos = palletizeFormService.queryByParam(queryCmd);
        if (CollUtil.isEmpty(formDtos)) {
            log.info("【组盘解绑】未找到容器 [{}] 对应的活跃组盘单，无需解绑", containerCode);
            return ResultWrapper.buildFailure("【组盘解绑】未找到容器 [{}] 对应的活跃组盘单，无需解绑", containerCode);
        }

        List<UUID> ids = formDtos.stream()
                .map(PalletizeFormDto::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            log.warn("【组盘解绑】容器 [{}] 关联的组盘单 ID 全部为空，跳过解绑", containerCode);
            return ResultWrapper.buildSuccess();
        }

        log.debug("【组盘解绑】准备取消 {} 个组盘单，ID 列表：{}", ids.size(), ids);
        palletizeAppService.cancelByIds(ids);
        log.info("【组盘解绑】成功取消 {} 个组盘单，容器编码：{}", ids.size(), containerCode);
        return ResultWrapper.buildSuccess();
    }

    @Override
    public List<AsnItemExtDto> findItemInfo(PutAwayCombineDto putAwayCombineDto) {
        String skuCode = putAwayCombineDto.getSkuCode();
        String businessOrderNo = putAwayCombineDto.getBusinessOrderNo();

        AsnItemExtExQuery queryParam = new AsnItemExtExQuery();
        queryParam.setBusinessOrderNo(businessOrderNo);
        if (StrUtil.isNotBlank(skuCode)) {
            queryParam.setSkuCode(skuCode);
        }

        List<AsnItemExtDto> asnItemExtEntities = asnItemExtService.queryListWithAsnHeader(queryParam);
        if (CollUtil.isEmpty(asnItemExtEntities)) {
            throw new BusinessException("收货明细查询异常: 未找到物料 [{}] 收料明细记录，请检查填写的信息是否正确！", skuCode);
        }
        return asnItemExtEntities;
    }

    @Override
    public ContainerDto scanTray(PutAwayCombineDto putAwayCombineDto) {
        if (putAwayCombineDto == null) {
            log.warn("【扫描托盘】收到空参数请求");
            throw new BusinessException("请求参数不能为空");
        }

        String containerCode = putAwayCombineDto.getContainerCode();
        String businessOrderNo = putAwayCombineDto.getBusinessOrderNo();
        String skuCode = putAwayCombineDto.getSkuCode();

        if (StrUtil.isBlank(containerCode)) {
            log.warn("【扫描托盘】容器编码为空");
            throw new BusinessException("容器号不能为空");
        }
        if (StrUtil.isBlank(businessOrderNo)) {
            log.warn("【扫描托盘】业务单号为空");
            throw new BusinessException("订单号不能为空");
        }
        if (StrUtil.isBlank(skuCode)) {
            log.warn("【扫描托盘】SKU 编码为空");
            throw new BusinessException("物料编码 (SKU) 不能为空");
        }

        log.info("【扫描托盘】开始验证：容器编码={}, 业务单号={}, SKU 编码={}", containerCode, businessOrderNo, skuCode);

        AsnItemExQuery queryParam = new AsnItemExQuery();
        queryParam.setBusiness_form_no(businessOrderNo);
        queryParam.setSku_code(skuCode);

        List<AsnItemDto> asnItems = asnItemService.queryList(queryParam);
        if (CollUtil.isEmpty(asnItems)) {
            log.warn("【扫描托盘】未找到订单 [{}] 中物料 [{}] 的收货明细", businessOrderNo, skuCode);
            throw new BusinessException("收货明细查询异常：未找到物料 [{}] 收料明细记录，请检查填写的信息是否正确！", skuCode);
        }

        StorageLocationInventoryDto storage = storageLocationInventoryAppService.getStorageByContainerCode(null, containerCode);
        if (storage != null && LocationType.cubic.name().equals(storage.getLoc_type())) {
            String locationCode = storage.getLocation_code();
            log.warn("【扫描托盘】容器 [{}] 位于立库库位 [{}]，禁止用于收货上架", containerCode, locationCode);
            throw new BusinessException("托盘 [{}] 所在库位 [{}] 是立库库位，不可用于收货上架！", containerCode, locationCode);
        }

        List<ContainerMaterialDto> boundMaterials = containerMaterialAppService.queryByContainer(null, containerCode);
        if (CollUtil.isNotEmpty(boundMaterials)) {
            log.warn("【扫描托盘】容器 [{}] 已绑定 {} 条物料，禁止重复绑定", containerCode, boundMaterials.size());
            throw new BusinessException("绑定异常：载具托盘 [{}] 已存在物料绑定记录", containerCode);
        }

        ContainerDto containerDto = containerAppService.getByNo(containerCode);
        if (containerDto == null) {
            log.warn("【扫描托盘】系统中不存在容器 [{}]", containerCode);
            throw new BusinessException("容器 [{}] 不存在", containerCode);
        }

        log.info("【扫描托盘】验证通过，成功获取容器信息：容器编码={}", containerCode);
        return containerDto;
    }

    @Override
    public List<ToolingContainerDto> palletizeItemExtEntities(PalletizeItemExtQuery query) {
        List<PalletizeItemExtEntity> palletizeItemExtEntities =
                palletizeItemExtService.queryByPalletizeItemAndForm(query);

        if (CollectionUtils.isEmpty(palletizeItemExtEntities)) {
            return Collections.emptyList();
        }
        List<PalletizeItemExtEntity> unboundEntities = palletizeItemExtEntities.stream()
                .filter(entity -> {
                    String asnFormNo = entity.getAsn_form_no();
                    return StringUtils.isBlank(asnFormNo);
                })
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(unboundEntities)) {
            return Collections.emptyList();
        }

        List<ToolingContainerDto> toolingContainerDtos = new ArrayList<>();
        for (PalletizeItemExtEntity entity : unboundEntities) {
            ToolingContainerDto dto = new ToolingContainerDto();
            dto.setContainerCode(StringUtils.defaultString(entity.getContainer_code()));
            dto.setSkuCode(StringUtils.defaultString(entity.getSku_code()));
            dto.setSkuName(StringUtils.defaultString(entity.getSku_name()));
            dto.setBatchNo(StringUtils.defaultString(entity.getBatch_no()));
            Double primaryQty = entity.getPrimary_qty();
            dto.setQty(primaryQty != null ? BigDecimal.valueOf(primaryQty) : BigDecimal.ZERO);
            dto.setUnit(StringUtils.defaultString(entity.getPrimary_unit()));
            // 🔒 修复：使用 defaultString 避免 serial_no 为 null 时下游 NPE
            dto.setSerialNo(StringUtils.defaultString(entity.getSerial_no()));
            dto.setStorageMaterialId(String.valueOf(entity.getStorage_material_id()));
            dto.setId(String.valueOf(entity.getId()));
            toolingContainerDtos.add(dto);
        }
        return toolingContainerDtos;
    }

    @Override
    public void toolingOrderContainerBind(ToolingOrderContainerBindDto dto) {
        // ========== 1. 参数校验 ==========
        String businessOrderNo = dto.getBusinessOrderNo();
        if (StringUtils.isBlank(businessOrderNo)) {
            throw new BusinessException("业务订单号不能为空");
        }

        AsnItemExtExQuery queryParam = new AsnItemExtExQuery();
        queryParam.setBusinessOrderNo(businessOrderNo);
        List<AsnItemExtDto> asnItemExtDtos = asnItemExtService.queryListWithAsnHeader(queryParam);
        if (CollUtil.isEmpty(asnItemExtDtos)) {
            throw new BusinessException("收货明细查询异常: 未找到订单 [{}] 的收料明细记录，请检查填写的信息是否正确！", businessOrderNo);
        }

        List<ToolingContainerDto> containerDtos = dto.getContainerCodeList();
        if (CollUtil.isEmpty(containerDtos)) {
            throw new BusinessException("容器列表不能为空");
        }

        List<String> containerCodes = containerDtos.stream()
                .map(ToolingContainerDto::getContainerCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(containerCodes)) {
            throw new BusinessException("容器号列表不能为空");
        }

        // ========== 2. 查询组盘明细 ==========
        PalletizeItemExtQuery palletizeQuery = new PalletizeItemExtQuery();
        palletizeQuery.setContainerCodeList(containerCodes);
        List<PalletizeItemExtEntity> palletizeEntities =
                palletizeItemExtService.queryByPalletizeItemAndForm(palletizeQuery);

        Set<String> existingContainerCodes = palletizeEntities.stream()
                .map(PalletizeItemExtEntity::getContainer_code)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toSet());

        List<String> missingContainers = containerCodes.stream()
                .filter(code -> !existingContainerCodes.contains(code.trim()))
                .collect(Collectors.toList());

        if (!missingContainers.isEmpty()) {
            throw new BusinessException(
                    "容器绑定失败：以下容器在组盘明细中不存在，请检查容器号是否正确或是否已完成组盘操作！缺失容器: {}",
                    String.join(", ", missingContainers)
            );
        }

        // ========== 3. 建立容器编码 -> 组盘明细的映射 ==========
        // 🔒 修复：使用 safeTrim + 前置过滤，避免 toMap key 为 null
        Map<String, PalletizeItemExtEntity> containerCodeToEntityMap = palletizeEntities.stream()
                .filter(entity -> entity != null && StringUtils.isNotBlank(entity.getContainer_code()))
                .collect(Collectors.toMap(
                        entity -> entity.getContainer_code().trim(),
                        entity -> entity,
                        (e1, e2) -> e1
                ));

        // ========== 4. 批量查询物料 ==========
        List<String> serialNoList = palletizeEntities.stream()
                .map(PalletizeItemExtEntity::getSerial_no)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toList());

        List<StorageMaterialDto> storageMaterialDtoList = storageMaterialService.queryBySerialNo(serialNoList);

        if (CollUtil.isEmpty(storageMaterialDtoList)) {
            throw new BusinessException("未查询到任何有效物料，请检查序列号是否正确");
        }

        // 🔒 修复：serialNoToStorageMaterialMap - 安全 toMap
        Map<String, StorageMaterialDto> serialNoToStorageMaterialMap =
                Optional.ofNullable(storageMaterialDtoList)
                        .orElse(Collections.emptyList())
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(d -> {
                            String serial = d.getSerial_no();
                            return serial != null && !serial.trim().isEmpty();
                        })
                        .collect(Collectors.toMap(
                                d -> d.getSerial_no().trim(),
                                Function.identity(),
                                (d1, d2) -> d1
                        ));

        // ========== 5. 调用 MES 协议收料 ==========
        List<AsnDetailDto> asnDetailDtoList = mesProtocolFactory.getProtocolService().receiveMaterial(
                ReceiveMaterialDto.create()
                        .setManualType(ManualType.SKU)
                        .setAsnNo(businessOrderNo)
                        .setItemNo(null)
                        .setList(storageMaterialDtoList)
                        .build()
        );

        if (CollUtil.isEmpty(asnDetailDtoList)) {
            throw new BusinessException("MES 收料失败：未生成任何 ASN 明细");
        }

        // 🔒 修复：serialNoToAsnDetailMap - 安全 toMap
        Map<String, AsnDetailDto> serialNoToAsnDetailMap =
                Optional.ofNullable(asnDetailDtoList)
                        .orElse(Collections.emptyList())
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(d -> {
                            String serial = d.getSerial_no();
                            return serial != null && !serial.trim().isEmpty();
                        })
                        .collect(Collectors.toMap(
                                d -> d.getSerial_no().trim(),
                                Function.identity(),
                                (d1, d2) -> d1
                        ));

        // ========== 7. 构建更新数据（安全关联）==========
        List<PalletizeItemDto> updatePalletizeItemDtoList = new ArrayList<>();
        List<StorageMaterialDto> updateStorageMaterialDtoList = new ArrayList<>();
        List<AsnDetailDto> updateAsnDetailDtoList = new ArrayList<>();

        for (PalletizeItemExtEntity entity : palletizeEntities) {
            // 🔒 修复：安全获取 serial_no 和 container_code，避免 NPE
            String serialNoRaw = entity.getSerial_no();
            String containerCodeRaw = entity.getContainer_code();

            if (StringUtils.isBlank(serialNoRaw) || StringUtils.isBlank(containerCodeRaw)) {
                log.warn("⚠️ 组盘明细数据不完整，跳过 - entityId: {}, serial_no: {}, container_code: {}",
                        entity.getId(), serialNoRaw, containerCodeRaw);
                continue;  // ✅ 保持原逻辑：无效数据跳过
            }

            String serialNo = serialNoRaw.trim();
            String containerCode = containerCodeRaw.trim();

            // 1. 查找对应的 ASN 明细
            AsnDetailDto asnDetail = serialNoToAsnDetailMap.get(serialNo);
            if (asnDetail == null) {
                log.warn("⚠️ 未找到序列号 [{}] 对应的 ASN 明细，跳过容器 [{}]", serialNo, containerCode);
                continue;
            }
            StorageMaterialDto storageMaterial = serialNoToStorageMaterialMap.get(serialNo);
            if (storageMaterial == null) {
                continue;
            }

            // 2. 验证 UUID 格式（关键防护）
            Object storageMaterialIdObj = storageMaterial.getId();
            String storageMaterialIdStr = storageMaterialIdObj != null ? String.valueOf(storageMaterialIdObj).trim() : null;

            if (StringUtils.isBlank(storageMaterialIdStr)) {
                throw new BusinessException("ASN 明细 [序列号:{}] 的物料存储 ID 为空", serialNo);
            }

            // ✅ 安全转换 UUID（保留原 try-catch 逻辑）
            UUID storageMaterialId;
            try {
                storageMaterialId = UUID.fromString(storageMaterialIdStr);
            } catch (IllegalArgumentException e) {
                throw new BusinessException(
                        "物料存储 ID 格式错误（必须是 UUID）。序列号: {}, 当前值: {}",
                        serialNo, storageMaterialIdStr
                );
            }

            // 3. 构建更新 DTO
            PalletizeItemDto updateDto = new PalletizeItemDto();
            updateDto.setId(entity.getId());
            updateDto.setAsn_form_no(asnDetail.getAsn_no());
            updateDto.setAsn_item_no(asnDetail.getItem_no());
            updateDto.setAsn_detail_no(asnDetail.getDetail_no());
            updateDto.setStorage_material_id(storageMaterialId);

            storageMaterial.setId(storageMaterialId);
            storageMaterial.setBusiness_form_no(asnDetail.getBusiness_form_no());
            storageMaterial.setBusiness_item_no(asnDetail.getBusiness_item_no());

            asnDetail.setStorage_material_id(storageMaterialId);

            updatePalletizeItemDtoList.add(updateDto);
            updateStorageMaterialDtoList.add(storageMaterial);
            updateAsnDetailDtoList.add(asnDetail);
            log.debug("📦 准备更新组盘明细 - 容器: {}, 序列号: {}, ASN 明细: {}",
                    containerCode, serialNo, asnDetail.getId());
        }

        // ========== 8. 批量更新组盘明细 ==========
        if (!updatePalletizeItemDtoList.isEmpty()) {
            palletizeItemService.addOrUpdate(updatePalletizeItemDtoList);
            log.info("✅ 容器绑定成功 - 业务订单: {}, 绑定容器数: {}",
                    businessOrderNo, updatePalletizeItemDtoList.size());
            if (!updateStorageMaterialDtoList.isEmpty()) {
                storageMaterialService.addOrUpdate(updateStorageMaterialDtoList);
            }
            if (!updateAsnDetailDtoList.isEmpty()) {
                asnDetailAppService.addOrUpdate(updateAsnDetailDtoList);
            }
        } else {
            throw new BusinessException("无有效数据可更新，请检查物料与 ASN 明细的匹配关系");
        }
    }
}