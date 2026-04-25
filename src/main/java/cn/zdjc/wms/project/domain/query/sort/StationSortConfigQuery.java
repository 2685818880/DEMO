package cn.zdjc.wms.project.domain.query.sort;

import cn.zdjc.warehouse.ExQuery;
import cn.zdjc.wms.project.domain.entity.sort.StationSortConfigEntity;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.Data;

@Data
public class StationSortConfigQuery extends ExQuery {

    /**
     * 拣选站台
     */
    private String pickStation;

    /**
     * 缓存仓库
     */
    private String cacheHouse;

    /**
     * 绑定的业务信息编号(已项目而定)
     */
    private String bindInfoCode;

    /**
     * 申请设备点位
     */
    private String applyDevice;


    public Wrapper<StationSortConfigEntity> buildQueryWrapper() {
        QueryWrapper<StationSortConfigEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(pickStation)) {
            queryWrapper.eq("pick_station", pickStation);
        }
        if (StringUtils.isNotBlank(cacheHouse)) {
            queryWrapper.eq("cache_house", cacheHouse);
        }
        if (StringUtils.isNotBlank(bindInfoCode)) {
            queryWrapper.eq("bind_info_code", bindInfoCode);
        }
        if (StringUtils.isNotBlank(applyDevice)) {
            queryWrapper.eq("apply_device", applyDevice);
        }
        return queryWrapper;
    }
}
