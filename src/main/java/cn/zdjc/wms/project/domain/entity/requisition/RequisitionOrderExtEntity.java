package cn.zdjc.wms.project.domain.entity.requisition;

import cn.zdjc.wms.outbound.requisition.infrastructure.pojo.ReqOrderPojo;
import com.foeris.y.common.bean.BeanAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
@BeanAlias("wms_requisition_order")
public class RequisitionOrderExtEntity extends ReqOrderPojo {



}
