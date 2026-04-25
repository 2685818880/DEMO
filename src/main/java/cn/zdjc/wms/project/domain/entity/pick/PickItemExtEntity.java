package cn.zdjc.wms.project.domain.entity.pick;

import cn.zdjc.wms.outbound.pick.infrastructure.pojo.PickItemPojo;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.foeris.y.common.bean.BeanAlias;
import com.foeris.y.common.bean.BeanIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@BeanAlias("wms_pick_item")
@TableName("wms_pick_item")
public class PickItemExtEntity extends PickItemPojo {
    /**
     * 工作站编号
     */
    @BeanAlias("workstation_code")
    private String workstationCode;


    /**
     * 訂單箱號
     */
    @BeanAlias("order_container_code")
    private String orderContainerCode;


//    /**
//     * 当前已拣选数量
//     */
//    private Double picked_qty = 0D;

//    /**
//     * 物料库存占用数量
//     */
//    private Double primary_qty = 0D;

//    @TableField(exist = false)
//    @BeanIgnore
//    private Boolean modified = false;
}
