package cn.zdjc.wms.project.ai.service;

import cn.zdjc.wms.project.ai.dto.AiProviderVO;

import java.util.List;
import java.util.Map;

public interface AiModelService {

    List<AiProviderVO.AiModelVO> listByProvider(Long providerId);

    void toggleModel(Long id, Boolean enabled);

    Map<String, Object> testModel(Long id);
}
