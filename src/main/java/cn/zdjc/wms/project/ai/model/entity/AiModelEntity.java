package cn.zdjc.wms.project.ai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("wms_ai_model")
public class AiModelEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long providerId;
    private String name;
    private String type;
    private Boolean isEnabled;
    private String configJson;

    @TableLogic(value = "1", delval = "0")
    private Boolean isActive;

    private Date createDatetime;
}
