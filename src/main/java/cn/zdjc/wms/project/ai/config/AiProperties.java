package cn.zdjc.wms.project.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ai.api")
public class AiProperties {
    private String baseUrl = "http://localhost:8081";
    private int connectTimeout = 5000;
    private int readTimeout = 30000;
    private String apiKey = "";
}
