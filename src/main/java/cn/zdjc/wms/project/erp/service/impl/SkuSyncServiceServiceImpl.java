package cn.zdjc.wms.project.erp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.zdjc.warehouse.material.application.service.*;
import cn.zdjc.warehouse.material.domain.dto.SkuCategoryDto;
import cn.zdjc.warehouse.material.domain.dto.SkuDto;
import cn.zdjc.warehouse.material.domain.entity.SkuCategory;
import cn.zdjc.warehouse.material.domain.repository.SkuCategoryRepository;
import cn.zdjc.wms.common.application.dto.UnitConversionDto;
import cn.zdjc.wms.common.application.dto.UnitGroupDto;
import cn.zdjc.wms.common.application.dto.UnitInfoDto;
import cn.zdjc.wms.common.application.service.IUnitAppService;
import cn.zdjc.wms.common.application.service.IUnitConversionAppService;
import cn.zdjc.wms.common.application.service.IUnitGroupAppService;
import cn.zdjc.wms.common.domain.entity.UnitConversionEntity;
import cn.zdjc.wms.common.domain.entity.UnitGroupEntity;
import cn.zdjc.wms.common.domain.service.IUnitConvertService;
import cn.zdjc.wms.common.domain.service.IUnitGroupService;
import cn.zdjc.wms.common.infrastructure.enums.DateUnit;
import cn.zdjc.wms.common.utils.StringUtils;
import cn.zdjc.wms.project.erp.dto.sku.SkuItemDto;
import cn.zdjc.wms.project.erp.dto.sku.SkuMainDataDto;
import cn.zdjc.wms.project.erp.service.SkuSyncService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.foeris.y.common.bean.BeanUtl;
import com.foeris.y.common.exception.BusinessException;
import com.foeris.y.fairy.jdbc.annotation.Transaction;
import com.foeris.y.fairy.jdbc.connection.DatabaseExecuter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service
@Transaction
public class SkuSyncServiceServiceImpl implements SkuSyncService {

    @Autowired
    private SkuCategoryAppService skuCategoryAppService;

    @Autowired
    private SkuAppService skuAppService;

    @Autowired
    private IUnitAppService unitAppService;

    @Resource
    private IUnitGroupService unitGroupService;

    @Autowired
    private IUnitGroupAppService unitGroupAppService;

    @Resource
    private SkuCategoryRepository skuCategoryService;

    @Autowired
    private IUnitConversionAppService unitConversionAppService;

    @Autowired
    IUnitConvertService convertService;

    @Override
    public void handleSkuMaster(SkuMainDataDto dto) {
        List<SkuItemDto> materialDtoList = dto.getItems();

        if (CollectionUtil.isEmpty(materialDtoList)) {
            log.warn("ERP主数据同步：物料列表为空，跳过处理");
            return;
        }

        log.info("开始同步ERP主数据，共 {} 条物料", materialDtoList.size());

        for (SkuItemDto materialDto : materialDtoList) {
            String skuCode = materialDto.getSkuCode();
            try {
                log.debug("处理物料 [{}]: {}", skuCode, materialDto.getSkuName());

                // 1. 处理库存单位
                UnitInfoDto inventoryUnitInfoDto = unitAppService.queryUnitByName(materialDto.getSkuSerialFlag());
                if (ObjectUtil.isEmpty(inventoryUnitInfoDto)) {
                    inventoryUnitInfoDto = new UnitInfoDto();
                    inventoryUnitInfoDto.setUnitCode(materialDto.getSkuSerialFlag());
                    inventoryUnitInfoDto.setUnitName(materialDto.getSkuSerialFlag());
                    unitAppService.saveUnit(inventoryUnitInfoDto);
                    log.debug("新增库存单位: {}", materialDto.getSkuSerialFlag());
                }

                // 2. 处理辅助单位
                if (StringUtils.isNotBlank(materialDto.getAuxiliaryUnit())) {
                    UnitInfoDto auxiliaryUnitInfoDto = unitAppService.queryUnitByName(materialDto.getAuxiliaryUnit());
                    if (ObjectUtil.isEmpty(auxiliaryUnitInfoDto)) {
                        auxiliaryUnitInfoDto = new UnitInfoDto();
                        auxiliaryUnitInfoDto.setUnitName(materialDto.getAuxiliaryUnit());
                        auxiliaryUnitInfoDto.setUnitCode(materialDto.getAuxiliaryUnit());
                        unitAppService.saveUnit(auxiliaryUnitInfoDto);
                        log.debug("新增辅助单位: {}", materialDto.getAuxiliaryUnit());
                    }

                    // 3. 处理品类
                    List<SkuCategory> categoryList = skuCategoryService.getCheckSkuCategoryList(null, materialDto.getCategoryCode(), null);
                    SkuCategoryDto skuCategoryDto;
                    if (CollectionUtil.isNotEmpty(categoryList)) {
                        skuCategoryDto = new SkuCategoryDto();
                        BeanUtl.copyProperties(categoryList.get(0), skuCategoryDto);
                        log.debug("复用品类: {} ({})", materialDto.getCategoryName(), materialDto.getCategoryCode());
                    } else {
                        skuCategoryDto = skuCategoryAppService.addOrUpdateCategory(
                                new SkuCategoryDto(materialDto.getCategoryCode(), materialDto.getCategoryName(), null, null)
                        );
                        log.debug("新增品类: {} ({})", materialDto.getCategoryName(), materialDto.getCategoryCode());
                    }

                    // 4. 保存单位方案及换算率
                    String unitPlanId = saveUnitInfo(materialDto, inventoryUnitInfoDto.getId(), auxiliaryUnitInfoDto.getId());

                    // 5. 保存 SKU
                    SkuDto skuDto = new SkuDto();
                    SkuDto sku = skuAppService.getSkuByCode(materialDto.getSkuCode());
                    BeanUtl.copyProperties(materialDto, skuDto);
                    //存在传ID更新
                    if(sku!=null){
                        skuDto.setId(sku.getId());
                    }
                    skuDto.setCategory_code(materialDto.getCategoryCode());
                    skuDto.setCategory_name(materialDto.getCategoryName());
                    skuDto.setSku_code(materialDto.getSkuCode());
                    skuDto.setSku_name(materialDto.getSkuName());
                    skuDto.setUnit_plan_id(unitPlanId);
                    skuDto.setCategory_id(skuCategoryDto.getId());
                    skuDto.setDuration_of_validity(
                            StringUtils.isNotBlank(materialDto.getDurationOfValidity())
                                    ? Double.parseDouble(materialDto.getDurationOfValidity())
                                    : 0.0
                    );
                    skuDto.setValidity_date_unit(
                            StringUtils.isBlank(materialDto.getValidityDateUnit())
                                    ? DateUnit.Day
                                    : DateUnit.valueOf(materialDto.getValidityDateUnit())
                    );
                    skuDto.setBlock_state(!ObjectUtil.equal(materialDto.getBlockState(), "0"));
                    skuDto.setSku_serial_flag(false);

                    skuAppService.addOrUpdate(skuDto);
                    log.info("物料 [{}] 同步成功", skuCode);
                } else {
                    log.warn("物料 [{}] 辅助单位为空，跳过同步", skuCode);
                }
            } catch (Exception e) {
                log.error("同步物料 [{}] 时发生异常", skuCode, e);
                throw e; // 保持事务回滚行为
            }
        }

        log.info("ERP主数据同步完成，共处理 {} 条物料", materialDtoList.size());
    }

    private String saveUnitInfo(SkuItemDto materialDto, String unitId, String auxiliaryUnitId) {
        String unitName = materialDto.getSkuSerialFlag() + "和" + materialDto.getAuxiliaryUnit() + "单位方案";
        log.debug("处理单位方案: {}", unitName);

        List<UnitGroupEntity> unitGroupEntities = unitGroupService.queryUnitGroupByName(unitName);
        UnitGroupDto unitGroupDto;

        if (CollectionUtil.isEmpty(unitGroupEntities)) {
            unitGroupDto = new UnitGroupDto();
            unitGroupDto.setGroupName(unitName);
            unitGroupDto.setBaseUnitName(materialDto.getSkuSerialFlag());
            unitGroupDto.setPrimaryUnitName(materialDto.getSkuSerialFlag());
            unitGroupDto.setSecondUnitName(materialDto.getAuxiliaryUnit());
            unitGroupDto.setBaseUnitId(unitId);
            unitGroupDto.setPrimaryUnitId(unitId);
            unitGroupDto.setSecondUnitId(auxiliaryUnitId);
            unitGroupAppService.saveUnitGroup(unitGroupDto);
            log.debug("新增单位方案: {}", unitName);
        } else {
            unitGroupDto = new UnitGroupDto();
            BeanUtl.copyProperties(unitGroupEntities.get(0), unitGroupDto);
            if (!ObjectUtil.equals(unitGroupDto.getPrimaryUnitName(), materialDto.getSkuSerialFlag()) ||
                    !ObjectUtil.equals(unitGroupDto.getSecondUnitName(), materialDto.getAuxiliaryUnit())) {

                String sql = "SELECT count(1) FROM wms_sku WHERE sku_code <> '" + materialDto.getSkuCode() + "' and unit_plan_id = '" + unitGroupDto.getId() + "'";
                Integer integer = DatabaseExecuter.queryInteger(sql);
                if (integer != null && integer > 0) {
                    String errorMsg = "物料 [" + materialDto.getSkuCode() + "] 的单位方案已被其他物料使用，无法修改！";
                    log.error(errorMsg);
                    throw new BusinessException(errorMsg);
                }
                unitGroupDto.setPrimaryUnitName(materialDto.getSkuSerialFlag());
                unitGroupDto.setSecondUnitName(materialDto.getAuxiliaryUnit());
                unitGroupAppService.updateUnitGroup(unitGroupDto, unitGroupDto.getId());
                log.debug("更新单位方案: {}", unitName);
            }
        }

        // 保存单位换算
        if (StringUtils.isNotBlank(materialDto.getConversionRate())) {
            try {
                double exchRatio = Double.parseDouble(materialDto.getConversionRate());
                LambdaQueryWrapper<UnitConversionEntity> lambdaQueryWrapper = Wrappers.<UnitConversionEntity>lambdaQuery();
                lambdaQueryWrapper.eq(UnitConversionEntity::getUnitName, materialDto.getSkuSerialFlag());
                lambdaQueryWrapper.eq(UnitConversionEntity::getGroupId, unitGroupDto.getId());

                UnitConversionEntity unitConversionEntity = convertService.queryUnitConvertByWrapper(lambdaQueryWrapper);
                UnitConversionDto unitConversionDto = new UnitConversionDto();
                unitConversionDto.setGroupId(unitGroupDto.getId());
                unitConversionDto.setGroupName(unitGroupDto.getGroupName());
                unitConversionDto.setBaseUnitName(materialDto.getAuxiliaryUnit());
                unitConversionDto.setUnitId(unitId);
                unitConversionDto.setUnitName(materialDto.getSkuSerialFlag());
                unitConversionDto.setExchRatio(exchRatio);

                if (ObjectUtil.isEmpty(unitConversionEntity)) {
                    unitConversionAppService.save(unitConversionDto);
                    log.debug("新增单位换算: {} -> {} = {}",
                            materialDto.getAuxiliaryUnit(), materialDto.getSkuSerialFlag(), exchRatio);
                } else {
                    unitConversionAppService.updateById(unitConversionDto, unitConversionEntity.getId());
                    log.debug("更新单位换算: {} -> {} = {}",
                            materialDto.getAuxiliaryUnit(), materialDto.getSkuSerialFlag(), exchRatio);
                }
            } catch (NumberFormatException e) {
                log.warn("物料 [{}] 换算率 [{}] 格式无效，跳过换算配置", materialDto.getSkuCode(), materialDto.getConversionRate());
            }
        }

        return unitGroupDto.getId();
    }
}