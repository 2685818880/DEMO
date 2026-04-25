package cn.zdjc.wms.project.domain.dto.palletize;

import cn.zdjc.wms.common.annotation.Comment;
import cn.zdjc.wms.definition.infrastructure.enums.PalletizeFormStatus;
import cn.zdjc.wms.definition.infrastructure.enums.PalletizeType;
import com.foeris.y.common.bean.BeanAlias;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * 组盘单扩展实体（用于查询、展示或导出场景）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PalletizeFormExtDto implements Serializable {

    /**
     * 仓库号
     */
    @Comment("仓库号")
    @BeanAlias("house_code")
    private String houseCode;

    /**
     * 托盘号
     */
    @Comment("托盘号")
    @BeanAlias("container_code")
    private String containerCode;

    /**
     * 设备号
     */
    @Comment("设备号")
    @BeanAlias("device_code")
    private String deviceCode;

    /**
     * 开始时间
     */
    @Comment("开始时间")
    @BeanAlias("start_time")
    private Date startTime;

    /**
     * 完成时间
     */
    @Comment("完成时间")
    @BeanAlias("finish_time")
    private Date finishTime;

    /**
     * 入库库位
     */
    @Comment("入库库位")
    @BeanAlias("storage_location")
    private String storageLocation;

    /**
     * 任务号
     */
    @Comment("任务号")
    @BeanAlias("task_no")
    private String taskNo;

    /**
     * 描述/备注
     */
    @Comment("描述")
    @BeanAlias("remark")
    private String remark;


    @Comment("组盘单据号")
    @BeanAlias("palletize_form_no")
    private String palletizeFormNo;

    @Comment("单据类型")
    @BeanAlias("palletize_form_type")
    private PalletizeType palletizeFormType;

    @Comment("单据状态")
    @BeanAlias("palletize_form_status")
    private PalletizeFormStatus palletizeFormStatus;


}