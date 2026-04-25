package cn.zdjc.wms.project.domain.entity.sort;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.foeris.y.fairy.jdbc.ddd.Domain;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("wms_station_sort_config")
public class StationSortConfigEntity extends Domain {



    /**
     * 拣选站台
     */
    private String pickStation;

    /**
     * 缓存仓库
     */
    private String cacheHouse;

    /**
     * 申请设备点位
     */
    private String applyDevice;

    /**
     * 绑定的业务信息编号(已项目而定)
     */
    private String bindInfoCode;

    /**
     * 最大可做业务信息数量
     */
    private Integer maxCanDoNum;

    /**
     * 最大缓存托盘数量
     */
    private Integer maxCacheTrayNum;

    /**
     * 发送托盘数量
     */
    private Integer sendTrayNum;

}
