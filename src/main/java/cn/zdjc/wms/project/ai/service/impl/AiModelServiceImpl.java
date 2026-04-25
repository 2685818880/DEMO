package cn.zdjc.wms.project.ai.service.impl;

import cn.zdjc.wms.project.ai.model.entity.AiModelEntity;
import cn.zdjc.wms.project.ai.model.entity.AiProviderEntity;
import cn.zdjc.wms.project.ai.repository.AiModelRepository;
import cn.zdjc.wms.project.ai.repository.AiProviderRepository;
import cn.zdjc.wms.project.ai.service.AiModelService;
import cn.zdjc.wms.project.ai.util.AesEncryptUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiModelServiceImpl implements AiModelService {

    private static final Logger log = LoggerFactory.getLogger(AiModelServiceImpl.class);

    private final AiModelRepository aiModelRepository;
    private final AiProviderRepository aiProviderRepository;
    private final AesEncryptUtil aesEncryptUtil;
    private final RestTemplate restTemplate;

    public AiModelServiceImpl(AiModelRepository aiModelRepository,
                              AiProviderRepository aiProviderRepository,
                              AesEncryptUtil aesEncryptUtil,
                              RestTemplate restTemplate) {
        this.aiModelRepository = aiModelRepository;
        this.aiProviderRepository = aiProviderRepository;
        this.aesEncryptUtil = aesEncryptUtil;
        this.restTemplate = restTemplate;
    }

    @Override
    public List<cn.zdjc.wms.project.ai.dto.AiProviderVO.AiModelVO> listByProvider(Long providerId) {
        List<AiModelEntity> entities = aiModelRepository.findByProviderId(providerId);
        if (entities == null) return Collections.emptyList();
        return entities.stream().map(m -> {
            cn.zdjc.wms.project.ai.dto.AiProviderVO.AiModelVO vo = new cn.zdjc.wms.project.ai.dto.AiProviderVO.AiModelVO();
            vo.setId(m.getId());
            vo.setName(m.getName());
            vo.setType(m.getType());
            vo.setIsEnabled(m.getIsEnabled());
            vo.setConfigJson(m.getConfigJson());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void toggleModel(Long id, Boolean enabled) {
        AiModelEntity entity = aiModelRepository.findById(id);
        if (entity != null) {
            entity.setIsEnabled(enabled);
            aiModelRepository.save(entity);
        }
    }

    @Override
    public Map<String, Object> testModel(Long id) {
        // 1. 查找模型
        AiModelEntity model = aiModelRepository.findById(id);
        if (model == null) {
            return Map.of("success", false, "message", "模型不存在");
        }

        // 2. 查找提供商
        AiProviderEntity provider = aiProviderRepository.findById(model.getProviderId()).orElse(null);
        if (provider == null) {
            return Map.of("success", false, "message", "所属提供商不存在");
        }

        // 3. 检查 API 地址
        String baseUrl = provider.getApiBaseUrl();
        if (!StringUtils.hasText(baseUrl)) {
            return Map.of("success", false, "message", "提供商 API 地址未配置");
        }

        // 4. 获取 API 密钥
        if (!StringUtils.hasText(provider.getApiKeyEncrypted())) {
            return Map.of("success", false, "message", "提供商未托管 API 密钥，请先托管密钥");
        }
        String apiKey;
        try {
            apiKey = aesEncryptUtil.decrypt(provider.getApiKeyEncrypted());
        } catch (Exception e) {
            log.error("API密钥解密失败: providerId={}", provider.getId(), e);
            return Map.of("success", false, "message", "API 密钥解密失败");
        }

        // 5. 发送测试请求
        String url = baseUrl.replaceAll("/+$", "") + "/chat/completions";
        String modelName = model.getName();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", "Hello")
        ));
        requestBody.put("max_tokens", 5);
        requestBody.put("stream", false);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            log.info("测试模型连接: provider={}, model={}, url={}", provider.getName(), modelName, url);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Map.of("success", true, "message", "连接成功");
            } else {
                return Map.of("success", false, "message",
                        "API 返回异常状态码: " + response.getStatusCodeValue());
            }
        } catch (RestClientException e) {
            String msg = e.getMessage();
            if (msg != null) {
                if (msg.contains("401")) {
                    return Map.of("success", false, "message", "认证失败: API 密钥无效");
                } else if (msg.contains("403")) {
                    return Map.of("success", false, "message", "授权失败: API 密钥无权限");
                } else if (msg.contains("404")) {
                    return Map.of("success", false, "message", "API 端点不存在，请检查 API 地址");
                } else if (msg.contains("429")) {
                    return Map.of("success", false, "message", "请求频率过高，请稍后重试");
                } else if (msg.contains("Model Not Exist") || msg.contains("model_not_found")) {
                    return Map.of("success", false, "message",
                            "模型名 \"" + modelName + "\" 不被 API 识别，请检查模型名称或联系提供商");
                } else if (msg.contains("Connection refused") || msg.contains("connect timed out")) {
                    return Map.of("success", false, "message",
                            "无法连接到 " + baseUrl + "，请检查网络或 API 地址");
                }
            }
            return Map.of("success", false, "message", "连接失败: " + msg);
        } catch (Exception e) {
            log.error("模型测试异常: modelId={}", id, e);
            return Map.of("success", false, "message", "测试异常: " + e.getMessage());
        }
    }
}
