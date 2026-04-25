package cn.zdjc.wms.project.domain.query.ws;

import cn.hutool.core.bean.BeanUtil;
import cn.zdjc.wms.project.domain.entity.ws.WorkstationExtEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkstationResp  {

    private String id;

    /**
     * 工作站编号
     */
    private String workstationCode;


    /**
     * 工作站名称
     */
    private String workstationName;

    /**
     * 工作站描述
     */
    private String workstationDescribe;

    /**
     * 是否开启
     */
    private String isOpen;

    /**
     * IP地址
     */
    private String workstationIp;

    /**
     * 工作站状态
     * 枚举: WorkstationStatusEnums
     */
    private String workstationStatus;

    /**
     * 工作站模式
     * 枚举: WorkstationModeEnums
     */
    private String workstationMode;

    /**
     * 操作人
     */
    private String operator;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastModifyTime;


    private String lastModifyBy;

    public static WorkstationResp formatFromDO(WorkstationExtEntity workstationExtEntity) {
        final WorkstationResp resp = new WorkstationResp();
        BeanUtil.copyProperties(workstationExtEntity, resp);
        return resp;
    }
}
