package cn.zdjc.wms.project.service.inbound;

import cn.zdjc.warehouse.container.domain.dto.ContainerDto;
import cn.zdjc.wms.project.domain.dto.asn.AsnItemExtDto;
import cn.zdjc.wms.project.domain.dto.inbound.*;
import cn.zdjc.wms.project.domain.query.asn.AsnItemExtExQuery;
import cn.zdjc.wms.project.domain.query.palletize.PalletizeItemExtQuery;
import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import com.foeris.y.common.result.PageResult;

import java.util.List;

public interface PjInboundService {

    List<OrderExtDto> orderNoInfoList(AsnItemExtExQuery query) ;

    PageResult<OrderExtDto> orderNoPageInfo(AsnItemExtExQuery query) ;

    /**
     * 获取订单明细
     */
    List<AsnItemExtDto> findItemInfo(PutAwayCombineDto putAwayCombineDto);


    List<String> orderNoList(String skuCode);


    /**
     * 收货组盘（单条）
     */
    void putAway(PutAwayCombineDto putAwayCombineDto);

    /**
     * 批量收货组盘
     */
    void batchPutAway(BatchPutAwayCombineDto batchPutAwayCombineDto);

    /**
     * 工装批量收货组盘
     */
    void toolingBatchPutAway(BatchPutAwayCombineDto batchPutAwayCombineDto) ;

    /**
     * 组盘解绑
     */
    ResultWrapper<Void> combineUnbind(BatchPutAwayCombineDto batchPutAwayCombineDto);



    /**
     * 扫描托盘获取容器信息
     */
    ContainerDto scanTray(PutAwayCombineDto putAwayCombineDto);

    void toolingOrderContainerBind(ToolingOrderContainerBindDto dto) ;

    List<ToolingContainerDto> palletizeItemExtEntities(PalletizeItemExtQuery query) ;

}