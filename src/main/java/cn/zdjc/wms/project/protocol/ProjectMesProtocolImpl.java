package cn.zdjc.wms.project.protocol;

import cn.zdjc.warehouse.ExtraAnnotation;
import cn.zdjc.warehouse.inventory.application.service.StorageMaterialAppService;
import cn.zdjc.warehouse.inventory.domain.dto.StorageMaterialDto;
import cn.zdjc.warehouse.inventory.infrastructure.enums.PackageLevel;
import cn.zdjc.warehouse.material.application.service.SkuAppService;
import cn.zdjc.warehouse.material.domain.dto.SkuDto;
import cn.zdjc.warehouse.material.domain.dto.SkuUnitConversionDto;
import cn.zdjc.wms.common.utils.StringUtils;
import cn.zdjc.wms.core.protocol.mes.impl.AbstractMesProtocol;
import cn.zdjc.wms.definition.application.dto.QueryMaterialDto;
import cn.zdjc.wms.definition.domain.dto.AdjustStockDto;
import cn.zdjc.wms.definition.domain.dto.AsnDto;
import cn.zdjc.wms.definition.domain.dto.AsnItemDto;
import cn.zdjc.wms.definition.domain.service.AsnItemService;
import cn.zdjc.wms.definition.domain.service.AsnService;
import cn.zdjc.wms.definition.infrastructure.PrimaryGenerator;
import cn.zdjc.wms.project.ProjectConfig;
import cn.zdjc.wms.project.erp.service.ReceiptConfirmSyncService;
import com.foeris.y.common.bean.BeanUtl;
import com.foeris.y.common.exception.BusinessException;
import com.foeris.y.common.result.MessageResult;
import com.foeris.y.common.result.ResultFactory;
import com.foeris.y.common.string.StringUtl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 项目定制的 MES 协议实现
 */
@Slf4j
@Service
public class ProjectMesProtocolImpl extends AbstractMesProtocol {

    @Resource
    private StorageMaterialAppService storageMaterialAppService;

    @Resource
    private SkuAppService skuAppService;

    @Resource
    private AsnService asnService;

    @Resource
    private AsnItemService asnItemService;

    @Resource
    private ReceiptConfirmSyncService receiptConfirmSyncService;

    @Override
    public List<StorageMaterialDto> queryBySerialNo(List<String> serialNos, String houseCode) {
        log.info("【MES协议】根据序列号查询物料信息开始，序列号数量: {}, 仓库编码: {}",
                serialNos == null ? 0 : serialNos.size(), houseCode);

        if (serialNos == null || serialNos.isEmpty()) {
            log.warn("【MES协议】序列号列表为空，返回空结果");
            return new ArrayList<>();
        }

        List<StorageMaterialDto> result = new ArrayList<>();
        for (String serialNo : serialNos) {
            if (StringUtl.isEmpty(serialNo)) {
                log.warn("【MES协议】跳过空序列号");
                continue;
            }

            try {
                log.debug("【MES协议】查询序列号: {}", serialNo);
                StorageMaterialDto viewDto = storageMaterialAppService.getStorageMaterialBySerialNo(serialNo);
                if (viewDto == null) {
                    String errorMsg = "ERP未同步物料SN[" + serialNo + "]信息!";
                    log.error("【MES协议】{}", errorMsg);
                    throw new BusinessException(errorMsg);
                }

                // ⚠️ 修复：原代码错误地传入 serialNos（List），应传入单个 serialNo
                Map<String, Object> extraMap = BeanUtl.getPropertiesWithAnnotation(serialNo, ExtraAnnotation.class);
                StorageMaterialDto dto = BeanUtl.copyProperties(viewDto, StorageMaterialDto.class);
                dto.setMap(extraMap);
                result.add(dto);
                log.debug("【MES协议】序列号 {} 查询成功", serialNo);
            } catch (BusinessException e) {
                // 业务异常直接抛出
                throw e;
            } catch (Exception e) {
                log.error("【MES协议】查询序列号 {} 时发生系统异常", serialNo, e);
                throw new BusinessException("查询序列号失败: " + e.getMessage());
            }
        }

        log.info("【MES协议】序列号查询完成，返回 {} 条记录", result.size());
        return result;
    }

    @Override
    public StorageMaterialDto queryBySkuCode(QueryMaterialDto dto) {
        log.info("【MES协议】根据SKU查询物料信息开始: skuCode={}, houseCode={}, qty={}",
                dto.getSku_code(), dto.getHouse_code(), dto.getQty());

        String skuCode = dto.getSku_code();
        String houseCode = dto.getHouse_code();
        String batchNo = dto.getBatch_no(); // 可能为 null

        if (StringUtils.isBlank(skuCode)) {
            throw new BusinessException("SKU编码不能为空");
        }

        SkuDto skuDto = skuAppService.getSkuByCode(skuCode);
        if (skuDto == null) {
            String errorMsg = "物料编码[" + skuCode + "]信息未同步!";
            log.error("【MES协议】{}", errorMsg);
            throw new BusinessException(errorMsg);
        }

        // 单位转换
        SkuUnitConversionDto conversionDto = skuAppService.conversionUnit(
                SkuUnitConversionDto.Builder.create()
                        .setSkuCode(skuCode)
                        .setOriginalQty(dto.getQty())
                        .setOriginalUnit(skuDto.getBase_unit_name())
                        .build()
        );

        String sn = PrimaryGenerator.getInstance().nextFormNo("SN");
        StorageMaterialDto result = StorageMaterialDto.create()
                .setHouse_code(houseCode)
                .setSerial_no(sn)
                .setSkuDto(skuDto)
                .setBatch_no(batchNo) // 允许 null
                .setPrimary_qty(conversionDto.getTargetQty())
                .setPrimary_unit(conversionDto.getTargetUnit())
                .setAuxiliary_qty(conversionDto.getOriginalQty())
                .setAuxiliary_unit(conversionDto.getOriginalUnit())
                .setAvailable_qty(conversionDto.getTargetQty())
                .setPackage_level(PackageLevel.NORMAL)
                .setPackage_amount(0)
                .setPackage_no(sn)
                .build();

        log.info("【MES协议】SKU查询完成，生成序列号: {}", sn);
        return result;
    }

    @Override
    public MessageResult receiveFeedback(String formNo) {
        log.info("【MES协议】收货反馈开始，收料单号: {}", formNo);
        if (!ProjectConfig.Fake_ERP) {
            return ResultFactory.getSuccess(null);
        }
        if (StringUtl.isEmpty(formNo)) {
            log.warn("【MES协议】收料单号为空");
            return ResultFactory.getError("收料单号不能为空");
        }

        AsnDto asnDto = asnService.queryByFormNo(formNo);
        if (asnDto == null) {
            String errorMsg = "收料单[" + formNo + "]信息不存在";
            log.error("【MES协议】{}", errorMsg);
            return ResultFactory.getError(errorMsg);
        }

        List<AsnItemDto> itemViewDtos = asnItemService.queryByFormNo(formNo);
        if (itemViewDtos == null || itemViewDtos.isEmpty()) {
            String errorMsg = "收料单[" + formNo + "]明细信息不存在";
            log.error("【MES协议】{}", errorMsg);
            return ResultFactory.getError(errorMsg);
        }

        // 过滤未完成过账的明细
        List<AsnItemDto> pendingItems = itemViewDtos.stream()
                .filter(AsnItemDto::hasSubmitNoComplete)
                .collect(Collectors.toList());

        if (pendingItems.isEmpty()) {
            String displayNo = StringUtl.isEmpty(asnDto.getBusiness_form_no()) ? asnDto.getForm_no() : asnDto.getBusiness_form_no();
            String msg = "收料单[" + displayNo + "]无可过账明细";
            log.info("【MES协议】{}", msg);
            return ResultFactory.getSuccess(msg);
        }

        try {
            if (!ProjectConfig.Fake_ERP) {
                receiptConfirmSyncService.handleReceiptConfirmSyncMaster(asnDto.getId());
            }
            log.info("【MES协议】收货反馈成功，收料单号: {}", formNo);
            return ResultFactory.getSuccess("收货反馈成功");
        } catch (BusinessException e) {
            log.error("【MES协议】收货反馈业务异常，单号: {}", formNo, e);
            return ResultFactory.getError("收货反馈失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("【MES协议】收货反馈系统异常，单号: {}", formNo, e);
            return ResultFactory.getError("收货反馈失败: 系统异常");
        }
    }

    @Override
    public void adjustFeedback(List<AdjustStockDto> list) {
        log.info("【MES协议】库存调整反馈开始，调整项数量: {}", list == null ? 0 : list.size());
        try {
            super.adjustFeedback(list);
            log.info("【MES协议】库存调整反馈完成");
        } catch (Exception e) {
            log.error("【MES协议】库存调整反馈异常", e);
            throw new BusinessException("库存调整反馈失败: " + e.getMessage());
        }
    }

    // --- 协议元信息 ---
    @Override
    public String getName() {
        return "TEST_WMS_实现服务";
    }

    @Override
    public String getType() {
        return "TEST_WMS";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "项目定制MES协议实现";
    }
}