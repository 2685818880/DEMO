package cn.zdjc.wms.project.service.asn.impl;

import cn.hutool.core.util.StrUtil;
import cn.zdjc.warehouse.strategy.application.allotPolicy.ExQueryBean;
import cn.zdjc.wms.project.domain.dto.asn.AsnExtDto;
import cn.zdjc.wms.project.domain.dto.asn.AsnItemExtDto;
import cn.zdjc.wms.project.domain.entity.asn.AsnExtEntity;
import cn.zdjc.wms.project.domain.query.asn.AsnExtExQuery;
import cn.zdjc.wms.project.domain.query.asn.AsnItemExtExQuery;
import cn.zdjc.wms.project.mapper.asn.AsnExtMapper;
import cn.zdjc.wms.project.repository.asn.AsnExtRepository;
import cn.zdjc.wms.project.service.asn.AsnExtService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foeris.y.common.result.PageResult;
import com.foeris.y.fairy.jdbc.connection.DatabaseExecuter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class AsnExtServiceImpl
        extends ServiceImpl<AsnExtMapper, AsnExtEntity>
        implements AsnExtService {

    @Resource
    private AsnExtRepository repository;


    @Override
    public List<AsnExtEntity> queryList(AsnExtExQuery param) {
        if (param == null) {
            log.debug("ASN 查询参数为空，返回全量数据");
            return this.list();
        }

        log.debug("执行 ASN 扩展查询，参数: {}", param);

        LambdaQueryWrapper<AsnExtEntity> query = new LambdaQueryWrapper<>();

        // 仓库编码
        if (StrUtil.isNotBlank(param.getHouseCode())) {
            query.eq(AsnExtEntity::getHouseCode, param.getHouseCode());
        }

        // 单据号（WMS 流水号）
        if (StrUtil.isNotBlank(param.getFormNo())) {
            query.like(AsnExtEntity::getFormNo, param.getFormNo());
        }

        // 来源业务单号（如 SAP PO）
        if (StrUtil.isNotBlank(param.getBusinessFormNo())) {
            query.like(AsnExtEntity::getBusinessFormNo, param.getBusinessFormNo());
        }

        // 单据状态
        if (param.getAsnStatus() != null) {
            query.eq(AsnExtEntity::getAsnStatus, param.getAsnStatus());
        }

        // 供应商名称
        if (StrUtil.isNotBlank(param.getSupplierName())) {
            query.like(AsnExtEntity::getSupplierName, param.getSupplierName());
        }

        // 备注
        if (StrUtil.isNotBlank(param.getRemark())) {
            query.like(AsnExtEntity::getRemark, param.getRemark());
        }

        List<AsnExtEntity> result = this.list(query);
        log.debug("ASN 查询完成，返回 {} 条记录", result.size());
        return result;
    }
    @Override
    public PageResult<AsnExtDto> queryAsnPage(AsnExtExQuery query) {
        ExQueryBean exQuery = AsnExtExQuery.getQuery(query);
        return DatabaseExecuter.queryBeanPaged(
                exQuery.getSQL(),      // 完整SQL（不含分页）
                exQuery.getParams(),   // 参数Map
                query.getPage(),       // 页码
                query.getRow(),        // 每页行数
                AsnExtDto.class    // DTO类型
        );
    }

}