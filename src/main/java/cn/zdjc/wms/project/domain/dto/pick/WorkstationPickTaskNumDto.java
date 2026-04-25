package cn.zdjc.wms.project.domain.dto.pick;

import lombok.Data;

@Data
public class WorkstationPickTaskNumDto {

    /**
     * 工作站编号
     */
    private String workstationCode;

    /**
     * 任务数
     */
    private Integer taskNum;

}
