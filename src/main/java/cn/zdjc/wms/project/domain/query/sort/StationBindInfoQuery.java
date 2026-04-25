package cn.zdjc.wms.project.domain.query.sort;

import cn.zdjc.warehouse.ExQuery;
import cn.zdjc.wms.project.domain.entity.sort.StationBindInfoEntity;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.Data;

@Data
public class StationBindInfoQuery extends ExQuery {

    /**
     * 拣选站台
     */
    private String pickStation;

    /**
     * 站台配置id
     */
    private String sortConfigId;

    /**
     * 绑定的业务信息编号(已项目而定)
     */
    private String bindInfoCode;

    /**
     * 绑定状态
     * StationBindInfoStatusEnums
     */
    private String bindStatus;


    public Wrapper<StationBindInfoEntity> buildQueryWrapper() {
        QueryWrapper<StationBindInfoEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(pickStation)) {
            queryWrapper.eq("pick_station", pickStation);
        }
        if (StringUtils.isNotBlank(sortConfigId)) {
            queryWrapper.eq("sort_config_id", sortConfigId);
        }
        if (StringUtils.isNotBlank(bindInfoCode)) {
            queryWrapper.eq("bind_info_code", bindInfoCode);
        }
        if (StringUtils.isNotBlank(bindStatus)) {
            queryWrapper.eq("bind_status", bindStatus);
        }
        return queryWrapper;
    }
}
