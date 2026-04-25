package cn.zdjc.wms.project.domain.query.sort;

import cn.zdjc.warehouse.ExQuery;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StationCacheTrayBindResp extends ExQuery {

    private String id;

    /**
     * 拣选站台
     */
    private String pickStation;

    /**
     * 缓存仓库
     */
    private String cacheHouse;

    /**
     * 缓存设备
     */
    private String cacheDevice;

    /**
     * 容器号
     */
    private String containerCode;

    /**
     * 绑定的业务信息编号(以项目而定)
     */
    private String bindInfoCode;

    /**
     * 绑定时间
     */
    private LocalDateTime bindTime;

    /**
     * 离开时间
     */
    private LocalDateTime leaveTime;


    /**
     * 排序信息(以项目而定 可以是数字 可以是时间)
     */
    private String sortInfo;

    /**
     * 状态
     * CacheTrayBindStatusEnums
     */
    private String status;

    /**
     * 是否急单
     * IfEnums
     */
    private String isUrgent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastModifyTime;


    private String lastModifyBy;


}
