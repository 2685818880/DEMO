package cn.zdjc.wms.project.ai.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiProviderDTO {
    private String name;
    private String logo;
    private String apiBaseUrl;
    private String apiKey;
    private String remark;
    private List<ModelItem> models;

    @Data
    public static class ModelItem {
        private String name;
        private String type;
        private Boolean isEnabled;
    }
}
