package cn.zdjc.wms.project.service.asn;

import cn.zdjc.wms.project.domain.dto.asn.AsnExtDto;
import cn.zdjc.wms.project.domain.entity.asn.AsnExtEntity;
import cn.zdjc.wms.project.domain.query.asn.AsnExtExQuery;
import com.baomidou.mybatisplus.extension.service.IService;
import com.foeris.y.common.result.PageResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AsnExtService extends IService<AsnExtEntity> {
    List<AsnExtEntity> queryList(AsnExtExQuery param) ;

    PageResult<AsnExtDto> queryAsnPage(AsnExtExQuery query) ;
}
