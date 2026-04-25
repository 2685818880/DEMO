package cn.zdjc.wms.project.ai.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AiProviderVO {
    private Long id;
    private String name;
    private String logo;
    private String apiBaseUrl;
    private String apiKeyStatus;
    private String apiKeyMasked;
    private Date lastUpdateTime;
    private Boolean isEnabled;
    private String remark;
    private List<AiModelVO> models;

    @Data
    public static class AiModelVO {
        private Long id;
        private String name;
        private String type;
        private Boolean isEnabled;
        private String configJson;
    }
}
