package cn.zdjc.wms.project.ai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("wms_ai_provider")
public class AiProviderEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String logo;
    private String apiBaseUrl;
    private String apiKeyEncrypted;
    private String apiKeyStatus;
    private Date lastUpdateTime;
    private Boolean isEnabled;
    private String remark;

    @TableLogic(value = "1", delval = "0")
    private Boolean isActive;

    private String createBy;
    private Date createDatetime;
    private String lastModifyBy;
    private Date lastModifyDatetime;
}
