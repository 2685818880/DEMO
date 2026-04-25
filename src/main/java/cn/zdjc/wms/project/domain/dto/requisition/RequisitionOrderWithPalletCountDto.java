package cn.zdjc.wms.project.domain.dto.requisition;

import cn.zdjc.wms.definition.infrastructure.enums.AllotStatus;
import cn.zdjc.wms.definition.infrastructure.enums.FormStatus;
import cn.zdjc.wms.definition.infrastructure.enums.SubmitStatus;
import cn.zdjc.wms.outbound.wave.infrastructure.enums.WaveType;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 订单及托盘数量统计DTO
 */
@Builder
@Data
public class RequisitionOrderWithPalletCountDto implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // ========== 订单核心信息 ==========
    private String id;
    private String formNo;               // WMS本地出库订单编号
    private String businessFormNo;       // 其他系统业务单据号
    private FormStatus formStatus;       // 单据状态
    private AllotStatus allotStatus;     // 分配状态
    private SubmitStatus submitStatus;   // 过账状态
    
    // ========== 业务主体信息 ==========
    private String ownerCode;            // 货主编码
    private String ownerName;            // 货主名称
    private String customCode;           // 客户号
    private String customName;           // 客户名称
    
    // ========== 时间信息 ==========
    private Date startTime;              // 单据开始时间
    private Date finishTime;             // 单据完成时间
    private Date closeTime;              // 单据关闭时间
    
    // ========== 托盘统计信息 ==========
    private Long palletCount;            // 托盘总数（拣选容器数量）
    private Long activePalletCount;      // 活跃托盘数（未完成拣选）
    private Long completedPalletCount;   // 已完成托盘数
    
    // ========== 业务扩展 ==========
    private WaveType waveType;           // 波次类型
    private Integer priority;            // 优先级
    private String remark;               // 单据备注
}