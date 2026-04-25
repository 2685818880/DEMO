package cn.zdjc.wms.project.service.pick;


import cn.zdjc.wms.outbound.pick.infrastructure.enums.PickStatus;
import cn.zdjc.wms.project.domain.entity.pick.PickItemExtEntity;
import cn.zdjc.wms.project.domain.query.pick.PickItemExtQuery;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PickItemExtService extends IService<PickItemExtEntity> {

    List<PickItemExtEntity> queryByContainerCode(String containerCode, List<PickStatus> pickStatusList) ;

    List<PickItemExtEntity> queryByParam(PickItemExtQuery param) ;

    List<PickItemExtEntity> findPickAbleByContainer(PickItemExtQuery queryParam);

    PickItemExtEntity getPickItemById(String id);

    boolean updatePickedQtyAndStatus(String id, Double pickedQty, PickStatus pickStatus,String orderContainerCode) ;

    List<PickItemExtEntity> batchGetByIds(List<String> ids) ;

    void orderContainerExitOut(String businessFormNo) ;
}
