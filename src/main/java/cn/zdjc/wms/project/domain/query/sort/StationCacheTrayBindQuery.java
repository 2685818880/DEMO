package cn.zdjc.wms.project.domain.query.sort;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.zdjc.warehouse.ExQuery;
import cn.zdjc.wms.project.domain.entity.sort.StationCacheTrayBindEntity;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.Data;

import java.util.List;

@Data
public class StationCacheTrayBindQuery extends ExQuery {

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
     * 绑定的业务信息编号集合
     */
    private List<String> bindInfoCodeList;

    /**
     * 状态
     */
    private String status;


    /**
     * 状态
     */
    private String notStatus;


    /**
     * 状态集合
     */
    private List<String> statusList;

    public Wrapper<StationCacheTrayBindEntity> buildQueryWrapper() {
        QueryWrapper<StationCacheTrayBindEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(pickStation)) {
            queryWrapper.eq("pick_station", pickStation);
        }
        if (StringUtils.isNotBlank(cacheHouse)) {
            queryWrapper.eq("cache_house", cacheHouse);
        }
        if (StringUtils.isNotBlank(bindInfoCode)) {
            queryWrapper.eq("bind_info_code", bindInfoCode);
        }
        if (ObjectUtil.isNotEmpty(bindInfoCodeList)) {
            queryWrapper.in("bind_info_code", bindInfoCodeList);
        }
        if (StringUtils.isNotBlank(status)) {
            queryWrapper.eq("status", status);
        }
        if (StringUtils.isNotBlank(notStatus)) {
            queryWrapper.ne("status", notStatus);
        }
        if (CollUtil.isNotEmpty(statusList)) {
            queryWrapper.in("status", statusList);
        }
        return queryWrapper;
    }
}
