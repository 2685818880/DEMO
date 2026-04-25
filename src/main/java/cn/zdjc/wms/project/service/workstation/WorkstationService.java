package cn.zdjc.wms.project.service.workstation;


import cn.zdjc.wms.project.common.socket.dto.WorkstationInfoWsDto;
import cn.zdjc.wms.project.domain.dto.ws.WorkstationExtDto;
import cn.zdjc.wms.project.domain.entity.ws.WorkstationExtEntity;
import cn.zdjc.wms.project.domain.query.ws.WorkstationExtQuery;
import com.baomidou.mybatisplus.extension.service.IService;
import com.foeris.y.common.result.PageResult;

import java.util.List;

public interface WorkstationService extends IService<WorkstationExtEntity> {
    PageResult<WorkstationExtDto> findInfoByPage(WorkstationExtQuery query) ;
    List<WorkstationExtEntity> getWorkstationsByCode(WorkstationExtQuery query) ;
    PageResult<WorkstationExtEntity> queryPaged(WorkstationExtQuery exQuery) ;

    void updateStatus(WorkstationExtDto workstationExtDto);
    void updateWorkOrder(WorkstationExtDto workstationExtDto) ;
    WorkstationExtEntity findInfoByCodeAndMode(String workstationCode, String workstationMode);
    void checkWorkstation(WorkstationExtDto workstationExtDto, Boolean isUpdate) ;


    WorkstationExtDto getOne(WorkstationExtQuery query) ;

    void save(WorkstationExtDto dto) ;

    /**
     * webSocket触发工作站关闭
     *
     * @param workstationCode
     */
    void wsCloseStation(String workstationCode);

    /**
     * webSocket触发工作站开启
     *
     * @param workstationCode
     */
    void wsOpenStation(String workstationCode);
    /**
     * 推送ws信息
     *
     * @param workstationInfoWsDto
     */
//    void sendWsMsg(WorkstationInfoWsDto workstationInfoWsDto);
}