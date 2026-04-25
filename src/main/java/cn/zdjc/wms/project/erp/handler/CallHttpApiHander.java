package cn.zdjc.wms.project.erp.handler;

import cn.zdjc.wms.definition.domain.dto.AsnDto;
import cn.zdjc.wms.outbound.requisition.interfaces.web.dto.RequisitionItemWithWaveDto;

/**
 * @author xud
 */
public interface CallHttpApiHander {

    /**
     * 出库信息回填
     */
    void outboundInfoBackfill(RequisitionItemWithWaveDto.RequisitionOrderDto requisitionOrderDto) throws Exception;

    /**
     * 入库信息回填
     */
    void inboundInfoBackfill( AsnDto asnDto) throws Exception;



}
