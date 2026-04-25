package cn.zdjc.wms.project.domain.entity.requisition;

import cn.zdjc.wms.outbound.requisition.infrastructure.pojo.ReqItemPojo;
import com.foeris.y.common.bean.BeanAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@BeanAlias("wms_requisition_item")
public class RequisitionItemExtEntity extends ReqItemPojo {


}
