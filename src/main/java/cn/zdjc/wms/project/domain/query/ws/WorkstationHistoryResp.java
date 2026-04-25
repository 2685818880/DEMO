package cn.zdjc.wms.project.domain.query.ws;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


@Data
public class WorkstationHistoryResp  {

    private String uid;

    private String whse;

    private String toNbr;

    private String toItemNo;

    private String materialNbr;

    private BigDecimal qty;

    private String workstationCode;

    private String workstationType;

    private String operator;

    private String sourceContainerCode;

    private String targetContainerCode;

    private String taskType;

    private Date startTime;

    private Date endTime;


}
