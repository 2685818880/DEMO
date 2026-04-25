package cn.zdjc.wms.project.form.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("wms_form_data")
public class FormDataEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String formCode;
    private String businessKey;
    private String businessType;
    private String formDataJson;
    private String dataStatus = "DRAFT";

    private Boolean isActive = true;
    private Date createDatetime;
    private String createBy;
    private Date lastModifyDatetime;
    private String lastModifyBy;
}
