package cn.zdjc.wms.project.service.sku.impl;

import cn.zdjc.warehouse.inventory.infrastructure.util.SQLAppendUtil;
import cn.zdjc.warehouse.strategy.application.allotPolicy.ExQueryBean;
import cn.zdjc.wms.common.application.dto.UnitGroupDto;
import cn.zdjc.wms.common.application.service.IUnitGroupAppService;
import cn.zdjc.wms.common.utils.StringUtils;
import cn.zdjc.wms.project.domain.dto.sku.SkuExtDto;
import cn.zdjc.wms.project.domain.entity.sku.SkuExtEntity;
import cn.zdjc.wms.project.domain.query.sku.SkuExtQuery;
import cn.zdjc.wms.project.mapper.sku.SkuExtMapper;
import cn.zdjc.wms.project.repository.sku.SkuExtRepository;
import cn.zdjc.wms.project.service.sku.SkuExtService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foeris.y.common.jdbc.SqlUtl;
import com.foeris.y.common.result.PageResult;
import com.foeris.y.common.string.StringUtl;
import com.foeris.y.fairy.jdbc.connection.DatabaseExecuter;
import com.foreris.eris.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SKU 扩展信息服务实现类
 */
@Slf4j
@Service
public class SkuExtServiceImpl
        extends ServiceImpl<SkuExtMapper, SkuExtEntity>
        implements SkuExtService {

    @Resource
    private SkuExtRepository skuExtRepository;

    @Resource
    private IUnitGroupAppService iUnitGroupAppService;


    /**
     * 根据查询条件获取 SKU 扩展信息列表（不分页）
     */
    @Override
    public List<SkuExtDto> queryList(SkuExtQuery query) {
        log.debug("开始执行 SKU 扩展信息列表查询，参数: {}", query);
        ExQueryBean exQueryBean = buildQuery(query);
        List<SkuExtDto> result = DatabaseExecuter.queryBeanList(
                exQueryBean.getSQL(),
                exQueryBean.getParams(),
                SkuExtDto.class
        );
        log.debug("SKU 列表查询完成，返回 {} 条记录", result != null ? result.size() : 0);
        return result;
    }

    /**
     * 根据查询条件分页导出 SKU 扩展信息
     */
    @Override
    public PageResult<SkuExtDto> export(SkuExtQuery query) {
        log.debug("开始执行 SKU 扩展信息分页导出，参数: {}", query);

        // 构建带 WHERE 和 ORDER BY 的查询
        ExQueryBean exQueryBean = buildQuery(query);

        // 分页查询（注意：DatabaseExecuter.queryBeanPaged 需支持动态 SQL + 参数）
        PageResult<SkuExtDto> pageResult = DatabaseExecuter.queryBeanPaged(exQueryBean.getSQL(),
                query.getPage(), query.getRow(), SkuExtDto.class);

        log.debug("SKU 分页导出完成，当前页: {}, 总记录数: {}",
                query.getPage(), pageResult.getTotal());
        return pageResult;
    }

    /**
     * 根据 SKU 编码查询单条记录
     */
    @Override
    public SkuExtDto queryBySkuCode(String skuCode) {
        if (StringUtl.isEmpty(skuCode)) {
            throw new BusinessException("物料编码不能为空");
        }
        log.debug("根据 SKU 编码查询: {}", skuCode);
        SkuExtEntity entity = skuExtRepository.queryBySkuCode(skuCode);
        if (entity == null) {
            log.warn("SKU 编码不存在: {}", skuCode);
            throw new BusinessException("物料编码不存在");
        }
        String unitPlanId = entity.getUnit_plan_id();
        if(StringUtils.isBlank(unitPlanId)){
            log.warn("SKU 单位编码没有维护: {}", skuCode);
            throw new BusinessException("单位编码没有维护");
        }
        UnitGroupDto unitGroupDto = iUnitGroupAppService.queryUnitGroupById(unitPlanId);
        if (unitGroupDto == null) {
            log.warn("SKU 单位编码没有维护: {}", skuCode);
            throw new BusinessException("单位编码没有维护");
        }

        SkuExtDto dto = SkuExtDto.builder()
                .skuCode(entity.getSku_code())
                .skuName(entity.getSku_name())
                .unit(unitGroupDto.getBaseUnitName())
                .lastModifyBy(entity.getLast_modify_by())
                .build();

        log.debug("SKU 查询成功: {}", dto.getSkuCode());
        return dto;
    }

    /**
     * 构建动态查询 SQL 片段（WHERE + ORDER BY + 参数）
     */
    private ExQueryBean buildQuery(SkuExtQuery query) {
        StringBuilder whereSql = new StringBuilder(" where 1=1 ");
        StringBuilder orderSql = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        if (query != null) {
            // SKU 编码（前缀模糊）
            if (!StringUtl.isEmpty(query.getSkuCode())) {
                whereSql.append(" and sku_code like :skuCode ");
                params.put("skuCode", query.getSkuCode() + "%");
            }

            // SKU 名称（前缀模糊）
            if (!StringUtl.isEmpty(query.getSkuName())) {
                whereSql.append(" and sku_name like :skuName ");
                params.put("skuName", query.getSkuName() + "%");
            }

            // 区域（精确匹配）
            if (!StringUtl.isEmpty(query.getRegion())) {
                whereSql.append(" and region = :region ");
                params.put("region", query.getRegion());
            }

            // 最后修改人（前缀模糊）
            if (!StringUtl.isEmpty(query.getLastModifyBy())) {
                whereSql.append(" and last_modify_by like :lastModifyBy ");
                params.put("lastModifyBy", query.getLastModifyBy() + "%");
            }

            // 排序
            String orderBy = SQLAppendUtil.orderByString(query.getSidx(), query.getSord());
            if (!StringUtl.isEmpty(orderBy)) {
                orderSql.append(orderBy);
            }
        }

        String baseSql = "select " + SqlUtl.getColumns(SkuExtEntity.class)
                + " from " + SqlUtl.getTable(SkuExtEntity.class);

        return new ExQueryBean(baseSql, whereSql.toString(), orderSql.toString(), params);
    }
}