package cn.zdjc.wms.project.domain.query.pick;

import cn.zdjc.warehouse.inventory.infrastructure.enums.FlagStatus;
import cn.zdjc.warehouse.strategy.application.allotPolicy.ExQueryBean;
import cn.zdjc.wms.outbound.pick.infrastructure.enums.PickStatus;
import cn.zdjc.wms.outbound.pick.infrastructure.enums.PickType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
public class PickItemExtQuery extends ExQueryBean {
    /**
     * 明细ID
     */
    private UUID itemId;
    /**
     * 明细编号
     */
    private String itemNo;
    /**
     * 拣选类型
     */
    private PickType pickType;
    /**
     * 目标拣选区域
     */
    private String pickArea;
    /**
     * 拣选的目标站点
     */
    private String pickStation;
    /**
     * 关联详情ID
     */
    private UUID relationOrderId;
    /**
     * 关联单据号
     */
    private String relationOrderNo;
    /**
     * 关联详情ID
     */
    private UUID relationItemId;
    /**
     * 关联明细号
     */
    private String relationItemNo;
    /**
     * 业务单据号
     */
    private String bizFormNo;
    /**
     * 业务明细号
     */
    private String bizItemNo;
    /**
     * 包装条码
     */
    private String packageNo;
    /**
     * 齐套标识
     */
    private FlagStatus kittingFlag;
    /**
     * 是否允许拣选
     * 该 条件 只考虑 已拣选数量 与 计划拣出数量的关系，不考虑单据状态
     * true:未拣选完成，允许拣选，只查询允许拣选的
     * false:已拣选完成 不允许拣选,只拆查询拣选完成的
     * NULL:不考虑该条件
     */
    private Boolean pickAble;

    /**
     * 是否允许绑定拣选目标区域
     * 该条件只考虑 拣选区域 和拣选站台的情况，不考虑单据状态
     * true:未设定拣选区域和拣选站台，允许绑定拣选区域，只查询拣选区域为空，且拣选站台为空的记录
     * false:已设定拣选区域或已设定拣选站台的,只查询拣选区域不为空或者拣选站台不为空的
     * NULL:不考虑该条件
     */
    private Boolean areaBindAble;

    /**
     * 物料容器编号
     */
    private String containerCode;

    private String skuCode;

    /**
     * 仓库号
     */
    private String houseCode;

    /**
     * 拣选状态
     */
    private List<PickStatus> pickStatuses;

    /**
     * 下架单ID
     */
    private UUID outboundId;
}
