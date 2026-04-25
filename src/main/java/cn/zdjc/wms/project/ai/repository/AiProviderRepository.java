package cn.zdjc.wms.project.ai.repository;

import cn.zdjc.wms.project.ai.mapper.AiProviderMapper;
import cn.zdjc.wms.project.ai.model.entity.AiProviderEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AiProviderRepository {

    private final AiProviderMapper aiProviderMapper;

    public AiProviderRepository(AiProviderMapper aiProviderMapper) {
        this.aiProviderMapper = aiProviderMapper;
    }

    public Optional<AiProviderEntity> findById(Long id) {
        return Optional.ofNullable(aiProviderMapper.selectById(id));
    }

    public List<AiProviderEntity> findAll() {
        return aiProviderMapper.selectList(null);
    }

    public List<AiProviderEntity> findByKeyStatus(String status) {
        return aiProviderMapper.selectByKeyStatus(status);
    }

    public void save(AiProviderEntity entity) {
        if (entity.getId() == null) {
            aiProviderMapper.insert(entity);
        } else {
            aiProviderMapper.updateById(entity);
        }
    }

    public int deleteById(Long id) {
        return aiProviderMapper.deleteById(id);
    }
}
