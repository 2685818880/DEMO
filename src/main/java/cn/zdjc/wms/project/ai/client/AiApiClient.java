package cn.zdjc.wms.project.ai.client;

import cn.zdjc.wms.project.ai.config.AiProperties;
import cn.zdjc.wms.project.ai.dto.AiApiResponse;
import cn.zdjc.wms.project.ai.dto.AiRequests;
import cn.zdjc.wms.project.ai.dto.AiResponses;
import cn.zdjc.wms.project.ai.service.AiProviderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class AiApiClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiApiClient.class);

    private final RestTemplate restTemplate;
    private final AiProperties aiProperties;
    private final AiProviderService aiProviderService;

    public AiApiClient(RestTemplate restTemplate, AiProperties aiProperties,
                       AiProviderService aiProviderService) {
        this.restTemplate = restTemplate;
        this.aiProperties = aiProperties;
        this.aiProviderService = aiProviderService;
    }

    private String url(String path) {
        return aiProperties.getBaseUrl() + "/api/ai/v1/wms" + path;
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String apiKey = aiProviderService.getDefaultApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = aiProperties.getApiKey();
        }
        if (apiKey != null && !apiKey.isEmpty()) {
            headers.set("Authorization", "Bearer " + apiKey);
        }
        return headers;
    }

    private <T> AiApiResponse<T> post(String path, Object request,
                                       ParameterizedTypeReference<AiApiResponse<T>> responseType) {
        try {
            HttpHeaders headers = buildHeaders();
            HttpEntity<Object> entity = new HttpEntity<>(request, headers);

            ResponseEntity<AiApiResponse<T>> response = restTemplate.exchange(
                    url(path), HttpMethod.POST, entity, responseType);
            return response.getBody();
        } catch (Exception e) {
            LOGGER.error("AI API调用失败 [{}]: {}", path, e.getMessage(), e);
            AiApiResponse<T> error = new AiApiResponse<>();
            error.setSuccess(false);
            error.setCode("500");
            error.setMessage("AI服务调用失败: " + e.getMessage());
            return error;
        }
    }

    private <T> AiApiResponse<T> get(String path,
                                      ParameterizedTypeReference<AiApiResponse<T>> responseType) {
        try {
            HttpHeaders headers = buildHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<AiApiResponse<T>> response = restTemplate.exchange(
                    url(path), HttpMethod.GET, entity, responseType);
            return response.getBody();
        } catch (Exception e) {
            LOGGER.error("AI API调用失败 [{}]: {}", path, e.getMessage(), e);
            AiApiResponse<T> error = new AiApiResponse<>();
            error.setSuccess(false);
            error.setCode("500");
            error.setMessage("AI服务调用失败: " + e.getMessage());
            return error;
        }
    }

    public AiApiResponse<AiResponses.PredictionResult> predictInventory(
            AiRequests.InventoryPrediction request) {
        return post("/inventory/predict", request,
                new ParameterizedTypeReference<AiApiResponse<AiResponses.PredictionResult>>() {});
    }

    public AiApiResponse<AiResponses.PathResult> optimizePath(
            AiRequests.PathOptimization request) {
        return post("/path/optimize", request,
                new ParameterizedTypeReference<AiApiResponse<AiResponses.PathResult>>() {});
    }

    public AiApiResponse<AiResponses.AnomalyResult> detectAnomalies(
            AiRequests.AnomalyDetection request) {
        return post("/anomaly/detect", request,
                new ParameterizedTypeReference<AiApiResponse<AiResponses.AnomalyResult>>() {});
    }

    public AiApiResponse<AiResponses.AgentStatus> getAgentStatus() {
        return get("/status",
                new ParameterizedTypeReference<AiApiResponse<AiResponses.AgentStatus>>() {});
    }

    public AiApiResponse<AiResponses.HealthStatus> healthCheck() {
        return get("/health",
                new ParameterizedTypeReference<AiApiResponse<AiResponses.HealthStatus>>() {});
    }

    public AiApiResponse<Map<String, Object>> generateSchedule(Map<String, Object> request) {
        return post("/scheduling/generate", request,
                new ParameterizedTypeReference<AiApiResponse<Map<String, Object>>>() {});
    }

    public AiApiResponse<Map<String, Object>> executeAllocation(Map<String, Object> request) {
        return post("/allocation/execute", request,
                new ParameterizedTypeReference<AiApiResponse<Map<String, Object>>>() {});
    }
}
