package cn.zdjc.wms.project.form.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("wms_form_config")
public class FormConfigEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String code;
    private String description;
    private String configJson;
    private String formType;
    private String dbCode;
    private String tableName;
    private Integer version = 1;
    private Integer status = 1;

    private Boolean isActive = true;
    private Date createDatetime;
    private String createBy;
    private Date lastModifyDatetime;
    private String lastModifyBy;
}
