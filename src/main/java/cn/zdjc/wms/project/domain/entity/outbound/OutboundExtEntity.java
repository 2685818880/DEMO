package cn.zdjc.wms.project.domain.entity.outbound;

import cn.zdjc.bm.transjob.infrastructure.enums.TaskStatus;
import cn.zdjc.wms.transport.out.infrastructure.enums.OutBoundStatus;
import cn.zdjc.wms.transport.out.infrastructure.enums.OutboundType;
import cn.zdjc.wms.transport.out.infrastructure.pojo.OutboundPojo;
import com.baomidou.mybatisplus.annotation.TableName;
import com.foeris.y.common.bean.BeanAlias;
import com.foeris.y.common.bean.BeanIgnore;
import com.foeris.y.fairy.jdbc.ddd.Domain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * 出库单扩展实体（用于查询/展示）
 *
 * @version 1.0.0
 * @author liuyk
 * @since 2020/08/19
 */
@EqualsAndHashCode(callSuper = false)
@Data
@BeanAlias("wms_outbound")
@TableName("wms_outbound")
public class OutboundExtEntity /*extends OutboundPojo */{
    private static final long serialVersionUID = 1L;
    /**
     * 单号（对应数据库字段：form_no）
     */
    @BeanAlias("form_no")
    private String formNo;

    /**
     * 单据状态：就绪、下架、下架完成、回库、回库完成、完成
     * （对应数据库字段：form_status）
     */
    @BeanAlias("form_status")
    private OutBoundStatus formStatus;

    /**
     * 出库类型：整盘 或 拣选
     * （对应数据库字段：form_type）
     */
    @BeanAlias("form_type")
    private OutboundType formType;

    /**
     * 仓库号（对应数据库字段：house_code）
     */
    @BeanAlias("house_code")
    private String houseCode;

    /**
     * 托盘号（具体下架位置依据下架时的实际位置获取）
     * （对应数据库字段：container_code）
     */
    @BeanAlias("container_code")
    private String containerCode;

    /**
     * 原库位（对应数据库字段：location_code）
     */
    @BeanAlias("location_code")
    private String locationCode;

    /**
     * 下架任务号（对应数据库字段：out_task_no）
     */
    @BeanAlias("out_task_no")
    private String outTaskNo;

    /**
     * 下架站台（对应数据库字段：out_station）
     */
    @BeanAlias("out_station")
    private String outStation;

    /**
     * 下架状态（对应数据库字段：out_status）
     */
    @BeanAlias("out_status")
    private TaskStatus outStatus;

    /**
     * 下架开始时间（对应数据库字段：out_start_datetime）
     */
    @BeanAlias("out_start_datetime")
    private Date outStartDatetime;

    /**
     * 下架完成时间（对应数据库字段：out_finish_datetime）
     */
    @BeanAlias("out_finish_datetime")
    private Date outFinishDatetime;

    /**
     * 回库上架任务号（对应数据库字段：in_task_no）
     */
    @BeanAlias("in_task_no")
    private String inTaskNo;

    /**
     * 入库位置（对应数据库字段：in_location_code）
     */
    @BeanAlias("in_location_code")
    private String inLocationCode;

    /**
     * 入库状态（对应数据库字段：in_status）
     */
    @BeanAlias("in_status")
    private TaskStatus inStatus;

    /**
     * 入库开始时间（对应数据库字段：in_start_datetime）
     */
    @BeanAlias("in_start_datetime")
    private Date inStartDatetime;

    /**
     * 入库完成时间（对应数据库字段：in_finish_datetime）
     */
    @BeanAlias("in_finish_datetime")
    private Date inFinishDatetime;

    /**
     * 描述/备注（对应数据库字段：remark）
     */
    @BeanAlias("remark")
    private String remark;


    /**
     * 标记是否已修改（非持久化字段）
     */
//    @BeanIgnore
//    private Boolean modified = false;

    @BeanAlias("id")
    private UUID id;

    @BeanAlias("is_active")
    private Boolean isActive = true;

    @BeanAlias("create_datetime")
    private Date createDatetime;

    @BeanAlias("create_by")
    private String createBy;

    @BeanAlias("last_modify_datetime")
    private Date lastModifyDatetime;

    @BeanAlias("last_modify_by")
    private String lastModifyBy;
}