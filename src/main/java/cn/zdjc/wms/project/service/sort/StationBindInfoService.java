package cn.zdjc.wms.project.service.sort;

import cn.hutool.db.PageResult;
import cn.zdjc.wms.project.domain.entity.sort.StationBindInfoEntity;
import cn.zdjc.wms.project.domain.query.sort.StationBindInfoQuery;
import com.baomidou.mybatisplus.extension.service.IService;

public interface StationBindInfoService extends IService<StationBindInfoEntity> {
    /**
     * 分页查询
     *
     * @param query
     * @return
     */
    PageResult<StationBindInfoEntity> findPage(StationBindInfoQuery query);

    /**
     * 通过绑定业务信息删除数据
     *
     * @param bindInfo
     */
    boolean deleteForBindInfo(String bindInfo);

    /**
     * 删除拣选站台的绑定信息
     *
     * @param station
     */
    void deleteBindInfoByPickStation(String station);

    /**
     * 处理该站台预约绑定信息 变更绑定信息
     */
    void handleReservationBindToBind(String pickStation);

}
