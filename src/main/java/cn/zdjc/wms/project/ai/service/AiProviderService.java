package cn.zdjc.wms.project.ai.service;

import cn.zdjc.wms.project.ai.dto.AiProviderDTO;
import cn.zdjc.wms.project.ai.dto.AiProviderVO;

import java.util.List;

public interface AiProviderService {

    List<AiProviderVO> listProviders(String keyword);

    AiProviderVO getProviderDetail(Long id);

    void createProvider(AiProviderDTO dto);

    void updateProvider(Long id, AiProviderDTO dto);

    void deleteProvider(Long id);

    void hostApiKey(Long id, String apiKey);

    void updateApiKey(Long id, String apiKey);

    void deleteApiKey(Long id);

    void batchDelete(List<Long> ids);

    void batchUpdateApiKey(List<Long> ids, String apiKey);

    String getDefaultApiKey();

    void checkKeyStatus(Long id);
}
