package cn.zdjc.wms.project.ai.mapper;

import cn.zdjc.wms.project.ai.model.entity.AiModelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiModelMapper extends BaseMapper<AiModelEntity> {
    List<AiModelEntity> selectByProviderId(@Param("providerId") Long providerId);
}
