package cn.zdjc.wms.project.ai.service.impl;

import cn.zdjc.wms.project.ai.dto.AiProviderDTO;
import cn.zdjc.wms.project.ai.dto.AiProviderVO;
import cn.zdjc.wms.project.ai.model.entity.AiModelEntity;
import cn.zdjc.wms.project.ai.model.entity.AiProviderEntity;
import cn.zdjc.wms.project.ai.repository.AiModelRepository;
import cn.zdjc.wms.project.ai.repository.AiProviderRepository;
import cn.zdjc.wms.project.ai.service.AiProviderService;
import cn.zdjc.wms.project.ai.util.AesEncryptUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AiProviderServiceImpl implements AiProviderService {

    private static final Logger log = LoggerFactory.getLogger(AiProviderServiceImpl.class);

    private final AiProviderRepository aiProviderRepository;
    private final AiModelRepository aiModelRepository;
    private final AesEncryptUtil aesEncryptUtil;

    public AiProviderServiceImpl(AiProviderRepository aiProviderRepository,
                                  AiModelRepository aiModelRepository,
                                  AesEncryptUtil aesEncryptUtil) {
        this.aiProviderRepository = aiProviderRepository;
        this.aiModelRepository = aiModelRepository;
        this.aesEncryptUtil = aesEncryptUtil;
    }

    @Override
    public List<AiProviderVO> listProviders(String keyword) {
        List<AiProviderEntity> entities = aiProviderRepository.findAll();
        if (StringUtils.hasText(keyword)) {
            entities = entities.stream()
                    .filter(e -> e.getName() != null && e.getName().contains(keyword))
                    .collect(Collectors.toList());
        }
        return entities.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public AiProviderVO getProviderDetail(Long id) {
        AiProviderEntity entity = aiProviderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("提供商不存在: " + id));
        AiProviderVO vo = toVO(entity);
        vo.setModels(getModelsByProviderId(id));
        return vo;
    }

    @Override
    @Transactional
    public void createProvider(AiProviderDTO dto) {
        AiProviderEntity entity = new AiProviderEntity();
        entity.setName(dto.getName());
        entity.setLogo(dto.getLogo() != null ? dto.getLogo() : "");
        entity.setApiBaseUrl(dto.getApiBaseUrl());
        entity.setRemark(dto.getRemark() != null ? dto.getRemark() : "");
        entity.setIsEnabled(true);
        entity.setApiKeyStatus("not_hosted");

        if (StringUtils.hasText(dto.getApiKey())) {
            entity.setApiKeyEncrypted(aesEncryptUtil.encrypt(dto.getApiKey()));
            entity.setApiKeyStatus("hosted");
            entity.setLastUpdateTime(new Date());
        }

        aiProviderRepository.save(entity);
        saveModels(entity.getId(), dto);
    }

    @Override
    @Transactional
    public void updateProvider(Long id, AiProviderDTO dto) {
        AiProviderEntity entity = aiProviderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("提供商不存在: " + id));
        entity.setName(dto.getName());
        entity.setLogo(dto.getLogo() != null ? dto.getLogo() : "");
        entity.setApiBaseUrl(dto.getApiBaseUrl());
        entity.setRemark(dto.getRemark() != null ? dto.getRemark() : "");

        if (StringUtils.hasText(dto.getApiKey())) {
            entity.setApiKeyEncrypted(aesEncryptUtil.encrypt(dto.getApiKey()));
            entity.setApiKeyStatus("hosted");
            entity.setLastUpdateTime(new Date());
        }

        aiProviderRepository.save(entity);
        aiModelRepository.deleteByProviderId(id);
        saveModels(id, dto);
    }

    @Override
    public void deleteProvider(Long id) {
        aiProviderRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void hostApiKey(Long id, String apiKey) {
        AiProviderEntity entity = aiProviderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("提供商不存在: " + id));
        entity.setApiKeyEncrypted(aesEncryptUtil.encrypt(apiKey));
        entity.setApiKeyStatus("hosted");
        entity.setLastUpdateTime(new Date());
        aiProviderRepository.save(entity);
    }

    @Override
    @Transactional
    public void updateApiKey(Long id, String apiKey) {
        hostApiKey(id, apiKey);
    }

    @Override
    @Transactional
    public void deleteApiKey(Long id) {
        AiProviderEntity entity = aiProviderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("提供商不存在: " + id));
        entity.setApiKeyEncrypted(null);
        entity.setApiKeyStatus("not_hosted");
        entity.setLastUpdateTime(new Date());
        aiProviderRepository.save(entity);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        ids.forEach(this::deleteProvider);
    }

    @Override
    @Transactional
    public void batchUpdateApiKey(List<Long> ids, String apiKey) {
        ids.forEach(id -> updateApiKey(id, apiKey));
    }

    @Override
    public String getDefaultApiKey() {
        List<AiProviderEntity> providers = aiProviderRepository.findByKeyStatus("hosted");
        if (providers.isEmpty()) return "";
        return aesEncryptUtil.decrypt(providers.get(0).getApiKeyEncrypted());
    }

    @Override
    public void checkKeyStatus(Long id) {
        AiProviderEntity entity = aiProviderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("提供商不存在: " + id));
        if (!StringUtils.hasText(entity.getApiKeyEncrypted())) {
            entity.setApiKeyStatus("not_hosted");
        } else {
            try {
                String key = aesEncryptUtil.decrypt(entity.getApiKeyEncrypted());
                if (StringUtils.hasText(key)) {
                    entity.setApiKeyStatus("hosted");
                } else {
                    entity.setApiKeyStatus("expired");
                }
            } catch (Exception e) {
                entity.setApiKeyStatus("expired");
            }
        }
        aiProviderRepository.save(entity);
    }

    private AiProviderVO toVO(AiProviderEntity entity) {
        AiProviderVO vo = new AiProviderVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setLogo(entity.getLogo());
        vo.setApiBaseUrl(entity.getApiBaseUrl());
        vo.setApiKeyStatus(entity.getApiKeyStatus());
        vo.setApiKeyMasked(maskApiKey(entity.getApiKeyEncrypted()));
        vo.setLastUpdateTime(entity.getLastUpdateTime());
        vo.setIsEnabled(entity.getIsEnabled());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    private String maskApiKey(String encrypted) {
        if (!StringUtils.hasText(encrypted)) return "";
        try {
            String key = aesEncryptUtil.decrypt(encrypted);
            if (key.length() <= 6) return "****";
            return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
        } catch (Exception e) {
            return "****";
        }
    }

    private List<AiProviderVO.AiModelVO> getModelsByProviderId(Long providerId) {
        List<AiModelEntity> models = aiModelRepository.findByProviderId(providerId);
        if (models == null) return Collections.emptyList();
        return models.stream().map(m -> {
            AiProviderVO.AiModelVO mv = new AiProviderVO.AiModelVO();
            mv.setId(m.getId());
            mv.setName(m.getName());
            mv.setType(m.getType());
            mv.setIsEnabled(m.getIsEnabled());
            mv.setConfigJson(m.getConfigJson());
            return mv;
        }).collect(Collectors.toList());
    }

    private void saveModels(Long providerId, AiProviderDTO dto) {
        if (dto.getModels() != null) {
            dto.getModels().forEach(m -> {
                AiModelEntity me = new AiModelEntity();
                me.setProviderId(providerId);
                me.setName(m.getName());
                me.setType(m.getType() != null ? m.getType() : "");
                me.setIsEnabled(m.getIsEnabled() != null ? m.getIsEnabled() : true);
                aiModelRepository.save(me);
            });
        }
    }
}
