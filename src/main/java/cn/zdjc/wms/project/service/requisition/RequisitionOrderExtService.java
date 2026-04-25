package cn.zdjc.wms.project.service.requisition;


import cn.zdjc.wms.project.domain.dto.requisition.RequisitionOrderWithPalletCountDto;
import cn.zdjc.wms.project.domain.entity.requisition.RequisitionOrderExtEntity;
import cn.zdjc.wms.project.domain.query.requisition.RequisitionOrderExtExQuery;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface RequisitionOrderExtService extends IService<RequisitionOrderExtEntity> {

    RequisitionOrderExtEntity findByBusinessFormNo(String businessFormNo) ;
    RequisitionOrderWithPalletCountDto getOrderWithPalletCount(RequisitionOrderExtExQuery query) ;

}
