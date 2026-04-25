package cn.zdjc.wms.project.domain.dto.ws;

import cn.hutool.core.date.DatePattern;
import cn.zdjc.wms.common.annotation.Comment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;


@Data
public class WorkstationHistoryDTO  {

    @Comment("仓库")
    private String whse;

    @Comment("TO号")
    private String toNbr;

    @Comment("TO行号")
    private String toItemNo;

    @Comment("物料号")
    private String materialNbr;

    @Comment("数量")
    private BigDecimal qty;

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

    @Comment("开始时间")
    private Date startTime;

    @Comment("结束时间")
    private Date endTime;


    public void fieldCheck() {
        if (ObjectUtils.isEmpty(whse)) {
            throw new IllegalStateException("参数错误：whse不能为空");
        }
        if (ObjectUtils.isEmpty(toNbr)) {
            throw new IllegalStateException("参数错误：toNbr不能为空");
        }
        if (ObjectUtils.isEmpty(toItemNo)) {
            throw new IllegalStateException("参数错误：toItemNo不能为空");
        }
        if (ObjectUtils.isEmpty(materialNbr)) {
            throw new IllegalStateException("参数错误：materialNbr不能为空");
        }
        if (ObjectUtils.isEmpty(workstationCode)) {
            throw new IllegalStateException("参数错误：workstationCode不能为空");
        }
        if (ObjectUtils.isEmpty(workstationType)) {
            throw new IllegalStateException("参数错误：workstationType不能为空");
        }
        if (ObjectUtils.isEmpty(operator)) {
            throw new IllegalStateException("参数错误：operator不能为空");
        }
        if (ObjectUtils.isEmpty(sourceContainerCode)) {
            throw new IllegalStateException("参数错误：sourceContainerCode不能为空");
        }
        if (ObjectUtils.isEmpty(targetContainerCode)) {
            throw new IllegalStateException("参数错误：targetContainerCode不能为空");
        }
        if (ObjectUtils.isEmpty(taskType)) {
            throw new IllegalStateException("参数错误：taskType不能为空");
        }
        if (startTime == null) {
            throw new IllegalStateException("参数错误：startTime不能为空");
        }
        if (endTime == null) {
            throw new IllegalStateException("参数错误：endTime不能为空");
        }
    }

    static {
        try {
            mapping = XContentFactory.jsonBuilder()
                    .startObject()
                    .startObject("properties")
                    .startObject("uid").field("type", "long").endObject()
                    .startObject("whse").field("type", "keyword").endObject()
                    .startObject("toNbr").field("type", "keyword").endObject()
                    .startObject("toItemNo").field("type", "keyword").endObject()
                    .startObject("materialNbr").field("type", "keyword").endObject()
                    .startObject("qty").field("type", "double").endObject()
                    .startObject("workstationCode").field("type", "keyword").endObject()
                    .startObject("workstationType").field("type", "keyword").endObject()
                    .startObject("operator").field("type", "keyword").endObject()
                    .startObject("sourceContainerCode").field("type", "keyword").endObject()
                    .startObject("targetContainerCode").field("type", "keyword").endObject()
                    .startObject("taskType").field("type", "keyword").endObject()
                    .startObject("startTime").field("type", "date").field("format", DatePattern.NORM_DATETIME_PATTERN).endObject()
                    .startObject("endTime").field("type", "date").field("format", DatePattern.NORM_DATETIME_PATTERN).endObject()
                    .endObject()
                    .endObject();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Getter
    @JsonIgnore
    protected static XContentBuilder mapping;

}
