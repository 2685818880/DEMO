package cn.zdjc.wms.project.report.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("wms_report_config")
public class ReportConfigEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String reportName;
    private String reportCode;
    private String reportType = "TABLE";
    private String category;
    private String configJson;
    private String dbCode;
    private Integer version = 1;
    private Integer status = 0;
    private String remark;

    @TableField(exist = false)
    private Boolean isActive = true;
    @TableField(exist = false)
    private Date createDatetime;
    @TableField(exist = false)
    private String createBy;
    @TableField(exist = false)
    private Date lastModifyDatetime;
    @TableField(exist = false)
    private String lastModifyBy;
}
