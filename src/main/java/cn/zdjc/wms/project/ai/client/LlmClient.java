package cn.zdjc.wms.project.ai.client;

import cn.zdjc.wms.project.ai.model.entity.AiModelEntity;
import cn.zdjc.wms.project.ai.model.entity.AiProviderEntity;
import cn.zdjc.wms.project.ai.repository.AiModelRepository;
import cn.zdjc.wms.project.ai.repository.AiProviderRepository;
import cn.zdjc.wms.project.ai.util.AesEncryptUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class LlmClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(LlmClient.class);

    private static final String SYSTEM_PROMPT =
            "你是一个WMS仓库管理系统的查询意图解析器。分析用户的查询，提取以下信息：\n\n" +
            "1. queryType: 查询类型，必须是以下之一：\n" +
            "   - INVENTORY_SUMMARY: 库存汇总/库存分布/库存查询\n" +
            "   - SLOW_MOVING: 滞销品/呆滞品/慢动品查询\n" +
            "   - EXPIRY_WARNING: 临期品/过期品/效期预警\n" +
            "   - OVERSTOCK: 积压品/库存过剩/库存过多\n" +
            "   - SKU_QUERY: 物料档案/物料信息/产品档案/SKU查询\n" +
            "   - SKU_CATEGORY_QUERY: 物料类别/物料分类/产品类别/产品分类查询\n" +
            "   - LOCATION_QUERY: 库位/货位查询\n" +
            "   - CONTAINER_QUERY: 容器/托盘查询\n" +
            "   - DISPATCH_QUERY: 任务调度/调度信息查询\n" +
            "   - DISPATCH_JOB_QUERY: 任务信息/作业信息/作业任务查询\n\n" +
            "2. zone: 区域名称，如用户提到\"A区\"则返回\"A区\"，未提到则返回null\n" +
            "   注意：zone字段只包含区域名如\"A区\"、\"B区\"，不含多余文字\n\n" +
            "3. dwellDays: 滞销天数阈值，仅在queryType为SLOW_MOVING时有意义\n" +
            "   - 用户明确指定天数（如\"60天\"、\"3个月\"）则使用指定值\n" +
            "   - 未指定时默认为90\n\n" +
            "4. keyword: 搜索关键词，仅在以下类型时有效：\n" +
            "   - SKU_QUERY: 用户想查的具体物料名或编码\n" +
            "   - SKU_CATEGORY_QUERY: 用户想查的具体类别名\n" +
            "   - LOCATION_QUERY: 用户想查的具体库位号\n" +
            "   - CONTAINER_QUERY: 用户想查的容器编码\n" +
            "   - DISPATCH_QUERY / DISPATCH_JOB_QUERY: 相关查询条件\n" +
            "   若用户未指定具体搜索词则返回null\n\n" +
            "请只返回JSON格式，不要包含其他文字。";

    private final RestTemplate restTemplate;
    private final AiProviderRepository aiProviderRepository;
    private final AiModelRepository aiModelRepository;
    private final AesEncryptUtil aesEncryptUtil;
    private final ObjectMapper objectMapper;

    public LlmClient(RestTemplate aiRestTemplate,
                     AiProviderRepository aiProviderRepository,
                     AiModelRepository aiModelRepository,
                     AesEncryptUtil aesEncryptUtil,
                     ObjectMapper objectMapper) {
        this.restTemplate = aiRestTemplate;
        this.aiProviderRepository = aiProviderRepository;
        this.aiModelRepository = aiModelRepository;
        this.aesEncryptUtil = aesEncryptUtil;
        this.objectMapper = objectMapper;
    }

    /**
     * 解析用户的自然语言查询意图
     *
     * @param query 用户查询文本
     * @return 解析后的意图，失败时返回 null（调用方应降级到正则）
     */
    public ChatIntent parseIntent(String query) {
        try {
            // 1. 获取已配置的 Provider 和 Model
            AiProviderEntity provider = getDefaultProvider();
            if (provider == null) {
                LOGGER.warn("[LlmClient] 未找到已配置的API提供商，跳过LLM解析");
                return null;
            }
            String apiKey = aesEncryptUtil.decrypt(provider.getApiKeyEncrypted());
            if (apiKey == null || apiKey.isEmpty()) {
                LOGGER.warn("[LlmClient] API密钥为空，跳过LLM解析");
                return null;
            }

            AiModelEntity model = getEnabledModel(provider.getId());
            if (model == null) {
                LOGGER.warn("[LlmClient] 提供商 {} 下无已启用的模型", provider.getName());
                return null;
            }

            // 2. 构建请求
            String url = provider.getApiBaseUrl().replaceAll("/+$", "") + "/chat/completions";

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", model.getName());
            requestBody.put("temperature", 0);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
            messages.add(Map.of("role", "user", "content", query));
            requestBody.put("messages", messages);

            Map<String, Object> responseFormat = new LinkedHashMap<>();
            responseFormat.put("type", "json_object");
            requestBody.put("response_format", responseFormat);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 3. 调用 API
            long start = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);
            long elapsed = System.currentTimeMillis() - start;

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                LOGGER.error("[LlmClient] API返回异常: status={}", response.getStatusCode());
                return null;
            }

            // 4. 解析响应
            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText("");
            LOGGER.info("[LlmClient] LLM解析完成, query='{}', result='{}',耗时={}ms", query, content, elapsed);

            return objectMapper.readValue(content, ChatIntent.class);

        } catch (Exception e) {
            LOGGER.error("[LlmClient] LLM调用失败: {}", e.getMessage(), e);
            return null;
        }
    }

    private AiProviderEntity getDefaultProvider() {
        List<AiProviderEntity> providers = aiProviderRepository.findByKeyStatus("hosted");
        return providers.isEmpty() ? null : providers.get(0);
    }

    private AiModelEntity getEnabledModel(Long providerId) {
        List<AiModelEntity> models = aiModelRepository.findByProviderId(providerId);
        return models.stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsEnabled()))
                .findFirst()
                .orElse(null);
    }

    @Data
    public static class ChatIntent {
        private String type = "INVENTORY_SUMMARY";
        private String zone;
        private Integer dwellDays = 90;
        private String keyword;
    }
}
