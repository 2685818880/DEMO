package cn.zdjc.wms.project.form.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("wms_database_config")
public class DatabaseConfigEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String dbCode;
    private String dbName;
    private String dbType;
    private String driverClass;
    private String jdbcUrl;
    private String username;
    private String password;
    private Integer initialSize = 3;
    private Integer minIdle = 3;
    private Integer maxActive = 60;
    private Long maxWait = 60000L;
    private String validationQuery;
    private Integer enabled = 1;

    private Boolean isActive = true;
    private Date createDatetime;
    private String createBy;
    private Date lastModifyDatetime;
    private String lastModifyBy;
}
