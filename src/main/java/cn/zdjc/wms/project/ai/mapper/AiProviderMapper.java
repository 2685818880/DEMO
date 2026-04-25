package cn.zdjc.wms.project.ai.mapper;

import cn.zdjc.wms.project.ai.model.entity.AiProviderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiProviderMapper extends BaseMapper<AiProviderEntity> {
    List<AiProviderEntity> selectByKeyStatus(@Param("status") String status);
}
