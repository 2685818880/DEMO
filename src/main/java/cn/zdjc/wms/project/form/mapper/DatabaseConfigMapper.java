package cn.zdjc.wms.project.form.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.zdjc.wms.project.form.model.entity.DatabaseConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface DatabaseConfigMapper extends BaseMapper<DatabaseConfigEntity> {
    List<DatabaseConfigEntity> selectAllEnabled();
    DatabaseConfigEntity selectByCode(String dbCode);
    int checkCodeExists(String dbCode);
}
