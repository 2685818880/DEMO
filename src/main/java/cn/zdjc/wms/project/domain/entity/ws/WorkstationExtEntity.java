package cn.zdjc.wms.project.domain.entity.ws;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.foeris.y.common.bean.BeanAlias;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 工作站实体类
 */
//@EqualsAndHashCode(callSuper = true)
@Data
@TableName("wms_workstation")
public class WorkstationExtEntity implements Serializable /*extends Domain*/ {

    /**
     * 主键 - UUID字符串（数据库存储为VARCHAR/CHAR(36)）
     * 必须使用 @TableId 明确指定主键，否则 MyBatis-Plus 无法识别
     */
    @TableId(value = "id", type = IdType.INPUT)
    @BeanAlias("id")
    private String id;

    private static final long serialVersionUID = 1L;

    /**
     * 工作站编号（对应数据库字段：workstation_code）
     */
    @BeanAlias("workstation_code")
    private String workstationCode;

    /**
     * 工作站名称（对应数据库字段：workstation_name）
     */
    @BeanAlias("workstation_name")
    private String workstationName;

    /**
     * 工作站描述（对应数据库字段：workstation_describe）
     */
    @BeanAlias("workstation_describe")
    private String workstationDescribe;

    /**
     * 是否开启（对应数据库字段：is_open）
     */
    @BeanAlias("is_open")
    private String isOpen;

    /**
     * IP地址（对应数据库字段：workstation_ip）
     */
    @BeanAlias("workstation_ip")
    private String workstationIp;

    /**
     * 工作站状态（对应数据库字段：workstation_status）
     */
    @BeanAlias("workstation_status")
    private String workstationStatus;

    /**
     * 工作站模式（对应数据库字段：workstation_mode）
     */
    @BeanAlias("workstation_mode")
    private String workstationMode;

    /**
     * 操作人（对应数据库字段：operator）
     */
    @BeanAlias("operator")
    private String operator;


    /**
     * 是否激活
     */
    @BeanAlias("is_active")
    private Boolean isActive = true;

    /**
     * 创建时间
     */
    @BeanAlias("create_datetime")
    private Date createDatetime;

    /**
     * 创建人
     */
    @BeanAlias("create_by")
    private String createBy;

    /**
     * 最后修改时间
     */
    @BeanAlias("last_modify_datetime")
    private Date lastModifyDatetime;

    /**
     * 创建人
     */
    @BeanAlias("last_modify_by")
    private String lastModifyBy;




    /**
     * 订单号
     */
    @BeanAlias("order_no")
    private String orderNo;


    /**
     * 订单号
     */
    @BeanAlias("order_id")
    private String orderId;


    /**
     * 备注
     */
    @BeanAlias("remark")
    private String remark;

    /**
     * 标记是否已修改（非持久化字段，不参与数据库操作）
     */
//    @BeanIgnore
//    private Boolean modified = false;
}