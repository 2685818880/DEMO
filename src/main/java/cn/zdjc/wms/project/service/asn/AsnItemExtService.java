package cn.zdjc.wms.project.service.asn;

import cn.zdjc.wms.project.domain.dto.asn.AsnItemExtDto;
import cn.zdjc.wms.project.domain.entity.asn.AsnItemExtEntity;
import cn.zdjc.wms.project.domain.query.asn.AsnItemExtExQuery;
import com.baomidou.mybatisplus.extension.service.IService;
import com.foeris.y.common.result.PageResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AsnItemExtService extends IService<AsnItemExtEntity> {
    List<AsnItemExtEntity> queryList(AsnItemExtExQuery param) ;

    List<AsnItemExtDto> queryListWithAsnHeader(AsnItemExtExQuery param) ;

    PageResult<AsnItemExtDto> queryAsnItemPageList(AsnItemExtExQuery query) ;

    PageResult<AsnItemExtDto> queryAsnItemAndAsnPagePageList(AsnItemExtExQuery query) ;
}
