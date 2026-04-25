package cn.zdjc.wms.project.domain.query.ws;

import cn.hutool.core.date.DatePattern;
import cn.zdjc.wms.common.annotation.Comment;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;


@Data
public class WorkstationHistoryQuery  {

    @Comment("仓库")
    private String whse;

    @Comment("TO号")
    private String toNbr;

    @Comment("TO行号")
    private String toItemNo;

    @Comment("物料号")
    private String materialNbr;

    @Comment("工作站编号")
    private String workstationCode;

    @Comment("工作站类型")
    private String workstationType;

    @Comment("操作人")
    private String operator;

    @Comment("来源箱")
    private String sourceContainerCode;

    @Comment("目标箱")
    private String targetContainerCode;

    @Comment("任务类型")
    private String taskType;

    @Comment("开始时间开始")
    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    private Date startTime;

    @Comment("开始时间结束")
    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    private Date endTime;

}
