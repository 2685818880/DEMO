//package cn.zdjc.wms.project.service.sort.impl;
//
//import cn.hutool.core.collection.CollUtil;
//
//import cn.hutool.db.PageResult;
//import cn.zdjc.wms.project.domain.entity.sort.StationBindInfoEntity;
//import cn.zdjc.wms.project.domain.enums.sort.StationBindInfoStatusEnums;
//import cn.zdjc.wms.project.domain.query.sort.StationBindInfoQuery;
//import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
//import com.baomidou.mybatisplus.core.toolkit.StringUtils;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import org.springframework.stereotype.Service;
//
//import java.util.Comparator;
//import java.util.List;
//
//@Service
//public class StationBindInfoServiceImpl extends ServiceImpl<StationBindInfoMapper, StationBindInfoEntity> implements StationBindInfoService {
//    /**
//     * 分页查询
//     *
//     * @param query
//     * @return
//     */
//    @Override
//    public PageResult<StationBindInfoEntity> findPage(StationBindInfoQuery query) {
//        Page<StationBindInfoEntity> pageParam = new Page<>(query.getCurrent(), query.getSize());
//        Page<StationBindInfoEntity> pageList = this.page(pageParam, query.buildQueryWrapper());
//        return PageResult.<StationBindInfoEntity>builder().list(pageList.getRecords()).current(pageList.getCurrent()).size(pageList.getSize()).total(pageList.getTotal()).build();
//    }
//
//    /**
//     * 通过绑定业务信息删除数据
//     *
//     * @param bindInfo
//     */
//    @Override
//    public boolean deleteForBindInfo(String bindInfo) {
//        if (StringUtils.isNotBlank(bindInfo)) {
//
//            StationBindInfoQuery stationBindInfoQuery = new StationBindInfoQuery();
//            stationBindInfoQuery.setBindInfoCode(bindInfo);
//            stationBindInfoQuery.setBindStatus(StationBindInfoStatusEnums.BIND.getCode());
//            if (this.exists(stationBindInfoQuery.buildQueryWrapper())) {
//                StationBindInfoQuery deleteStationBindInfoQuery = new StationBindInfoQuery();
//                deleteStationBindInfoQuery.setBindInfoCode(bindInfo);
//                this.remove(deleteStationBindInfoQuery.buildQueryWrapper());
//                return true;
//            } else {
//                StationBindInfoQuery deleteStationBindInfoQuery = new StationBindInfoQuery();
//                deleteStationBindInfoQuery.setBindInfoCode(bindInfo);
//                this.remove(deleteStationBindInfoQuery.buildQueryWrapper());
//                return true;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * 删除拣选站台的绑定信息
//     *
//     * @param station
//     */
//    @Override
//    public void deleteBindInfoByPickStation(String station) {
//        StationBindInfoQuery stationBindInfoQuery = new StationBindInfoQuery();
//        stationBindInfoQuery.setPickStation(station);
//        stationBindInfoQuery.setBindStatus(StationBindInfoStatusEnums.BIND.getCode());
//        this.remove(stationBindInfoQuery.buildQueryWrapper());
//    }
//
//    /**
//     * 处理该站台预约绑定信息 变更绑定信息
//     *
//     * @param pickStation
//     */
//    @Override
//    public void handleReservationBindToBind(String pickStation) {
//
//        UpdateWrapper<StationBindInfoEntity> updateWrapper = new UpdateWrapper<>();
//        updateWrapper.eq("pick_station", pickStation);
//        updateWrapper.eq("bind_status", StationBindInfoStatusEnums.RESERVATION_BIND.getCode());
//        updateWrapper.set("bind_status", StationBindInfoStatusEnums.BIND.getCode());
//        this.update(updateWrapper);
//    }
//}
