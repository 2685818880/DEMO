package cn.zdjc.wms.project.service.palletize.impl;

import cn.zdjc.warehouse.strategy.application.allotPolicy.ExQueryBean;
import cn.zdjc.wms.project.domain.dto.asn.AsnExtDto;
import cn.zdjc.wms.project.domain.dto.palletize.PalletizeFormExtDto;
import cn.zdjc.wms.project.domain.entity.palletize.PalletizeFormExtEntity;
import cn.zdjc.wms.project.domain.entity.palletize.PalletizeItemExtEntity;
import cn.zdjc.wms.project.domain.query.asn.AsnExtExQuery;
import cn.zdjc.wms.project.domain.query.palletize.PalletizeFormExtExQuery;
import cn.zdjc.wms.project.mapper.palletize.PalletizeFormExtMapper;
import cn.zdjc.wms.project.mapper.palletize.PalletizeItemExtMapper;
import cn.zdjc.wms.project.repository.outbound.OutboundExtRepository;
import cn.zdjc.wms.project.repository.palletize.PalletizeFormExtRepository;
import cn.zdjc.wms.project.service.palletize.PalletizeFormExtService;
import cn.zdjc.wms.project.service.palletize.PalletizeItemExtService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foeris.y.common.result.PageResult;
import com.foeris.y.fairy.jdbc.connection.DatabaseExecuter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class PalletizeFormExtServiceImpl
        extends ServiceImpl<PalletizeFormExtMapper, PalletizeFormExtEntity>
        implements PalletizeFormExtService {
    @Resource
    private PalletizeFormExtRepository repository;

//    @Override
//    public PageResult<PalletizeFormExtDto> queryAsnPage(PalletizeFormExtExQuery query) {
//        ExQueryBean exQuery = AsnExtExQuery.getQuery(query);
//        return DatabaseExecuter.queryBeanPaged(
//                exQuery.getSQL(),      // 完整SQL（不含分页）
//                exQuery.getParams(),   // 参数Map
//                query.getPage(),       // 页码
//                query.getRow(),        // 每页行数
//                AsnExtDto.class    // DTO类型
//        );
//    }
}
