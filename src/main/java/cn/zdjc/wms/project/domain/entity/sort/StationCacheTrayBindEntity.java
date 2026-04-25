package cn.zdjc.wms.project.domain.entity.sort;

import cn.zdjc.wms.project.common.enums.IfEnums;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.foeris.y.fairy.jdbc.ddd.Domain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("wms_station_cache_tray_bind_record")
public class StationCacheTrayBindEntity extends Domain {


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
     * 目的地
     */
    private String locationTo;

    /**
     * 是否急单
     * IfEnums
     */
    private String isUrgent = IfEnums.NO.getCode();
}
