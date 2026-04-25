package cn.zdjc.wms.project.service.requisition;


import cn.zdjc.wms.project.domain.dto.asn.AsnItemExtDto;
import cn.zdjc.wms.project.domain.dto.inbound.OrderExtDto;
import cn.zdjc.wms.project.domain.dto.requisition.RequisitionItemExtDto;
import cn.zdjc.wms.project.domain.entity.requisition.RequisitionItemExtEntity;
import cn.zdjc.wms.project.domain.query.asn.AsnItemExtExQuery;
import cn.zdjc.wms.project.domain.query.requisition.RequisitionItemExtExQuery;
import com.baomidou.mybatisplus.extension.service.IService;
import com.foeris.y.common.result.PageResult;

import java.util.List;

public interface RequisitionItemExtService extends IService<RequisitionItemExtEntity> {

    List<RequisitionItemExtDto> queryListWithAsnHeader(RequisitionItemExtExQuery param) ;

    PageResult<RequisitionItemExtDto> queryItemAndOrderListWithAsnHeader(RequisitionItemExtExQuery query) ;

    List<OrderExtDto> orderNoListInfo(RequisitionItemExtExQuery query);

    PageResult<OrderExtDto> orderNoPageInfo(RequisitionItemExtExQuery query) ;
}
