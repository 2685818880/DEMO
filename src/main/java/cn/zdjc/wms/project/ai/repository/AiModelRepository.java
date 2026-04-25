package cn.zdjc.wms.project.ai.repository;

import cn.zdjc.wms.project.ai.mapper.AiModelMapper;
import cn.zdjc.wms.project.ai.model.entity.AiModelEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AiModelRepository {

    private final AiModelMapper aiModelMapper;

    public AiModelRepository(AiModelMapper aiModelMapper) {
        this.aiModelMapper = aiModelMapper;
    }

    public AiModelEntity findById(Long id) {
        return aiModelMapper.selectById(id);
    }

    public List<AiModelEntity> findByProviderId(Long providerId) {
        return aiModelMapper.selectByProviderId(providerId);
    }

    public void save(AiModelEntity entity) {
        if (entity.getId() == null) {
            aiModelMapper.insert(entity);
        } else {
            aiModelMapper.updateById(entity);
        }
    }

    public void deleteByProviderId(Long providerId) {
        aiModelMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiModelEntity>()
                        .eq(AiModelEntity::getProviderId, providerId));
    }
}
