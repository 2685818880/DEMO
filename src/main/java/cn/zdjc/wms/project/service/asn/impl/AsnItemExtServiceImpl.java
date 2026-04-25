package cn.zdjc.wms.project.service.asn.impl;

import cn.hutool.core.util.StrUtil;
import cn.zdjc.warehouse.strategy.application.allotPolicy.ExQueryBean;
import cn.zdjc.wms.project.domain.dto.asn.AsnItemExtDto;
import cn.zdjc.wms.project.domain.entity.asn.AsnItemExtEntity;
import cn.zdjc.wms.project.domain.query.asn.AsnItemExtExQuery;
import cn.zdjc.wms.project.mapper.asn.AsnItemExtMapper;
import cn.zdjc.wms.project.service.asn.AsnItemExtService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foeris.y.common.result.PageResult;
import com.foeris.y.fairy.jdbc.connection.DatabaseExecuter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AsnItemExtServiceImpl
        extends ServiceImpl<AsnItemExtMapper, AsnItemExtEntity>
        implements AsnItemExtService {
//    @Resource
//    private AsnItemExtRepository repository;



    @Override
    public List<AsnItemExtEntity> queryList(AsnItemExtExQuery param) {
        if (param == null) {
            log.debug("ASN 明细查询参数为空，返回全量数据");
            return this.list();
        }

        log.debug("执行 ASN 明细扩展查询，参数: {}", param);

        LambdaQueryWrapper<AsnItemExtEntity> query = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(param.getAsnId())) {
            query.eq(AsnItemExtEntity::getAsnId, param.getAsnId());
        }
        if (StrUtil.isNotBlank(param.getAsnNo())) {
            query.eq(AsnItemExtEntity::getAsnNo, param.getAsnNo());
        }
        if (StrUtil.isNotBlank(param.getItemNo())) {
            query.eq(AsnItemExtEntity::getItemNo, param.getItemNo());
        }
        if (StrUtil.isNotBlank(param.getBusinessItemNo())) {
            query.like(AsnItemExtEntity::getBusinessItemNo, param.getBusinessItemNo());
        }
        if (StrUtil.isNotBlank(param.getSkuCode())) {
            query.eq(AsnItemExtEntity::getSkuCode, param.getSkuCode());
        }
        if (StrUtil.isNotBlank(param.getBatchNo())) {
            query.eq(AsnItemExtEntity::getBatchNo, param.getBatchNo());
        }
        if (StrUtil.isNotBlank(param.getFactory())) {
            query.eq(AsnItemExtEntity::getFactory, param.getFactory());
        }
        List<AsnItemExtEntity> result = this.list(query);
        log.debug("ASN 明细查询完成，返回 {} 条记录", result.size());
        return result;
    }

    @Override
    public List<AsnItemExtDto> queryListWithAsnHeader(AsnItemExtExQuery query) {
        if (query == null) {
            log.debug("ASN 明细关联主表查询参数为空，返回全量数据");
            return null;
        }
        log.debug("执行 ASN 明细关联主表查询，参数: {}", query);

        List<AsnItemExtDto> result = baseMapper.selectWithAsnHeader(query);
        log.debug("ASN 明细关联查询完成，返回 {} 条记录", result.size());
        return result;
    }

    @Override
    public PageResult<AsnItemExtDto> queryAsnItemAndAsnPagePageList(AsnItemExtExQuery query) {
        if (query == null) {
            query = new AsnItemExtExQuery();
        }
        query.setPage(query.getPage() != null && query.getPage() > 0 ? query.getPage() : 1);
        query.setRow(query.getRow() != null && query.getRow() > 0 ? query.getRow() : 20);

        log.info("ASN 明细关联分页查询 - 页码: {}, 每页: {}", query.getPage(), query.getRow());

        try {
            // 2. 查询当前页数据
            List<AsnItemExtDto> list = baseMapper.selectWithAsnHeader(query);
            int total = list.size();
            // 3. 构建分页结果
            PageResult<AsnItemExtDto> result = new PageResult<>(list, query.getPage(), query.getRow(), total);

            log.info("ASN 明细关联查询完成 - 总记录: {}, 当前页: {}, 返回: {} 条", total, query.getPage(), list.size());

            return result;

        } catch (Exception e) {
            log.error("ASN 明细关联分页查询异常", e);
            return null;
        }
    }


    @Override
    public PageResult<AsnItemExtDto> queryAsnItemPageList(AsnItemExtExQuery query) {

        ExQueryBean exQuery = AsnItemExtExQuery.getQuery(query);
        return DatabaseExecuter.queryBeanPaged(
                exQuery.getSQL(),      // 完整SQL（不含分页）
                exQuery.getParams(),   // 参数Map
                query.getPage(),       // 页码
                query.getRow(),        // 每页行数
                AsnItemExtDto.class    // DTO类型
        );
    }

}