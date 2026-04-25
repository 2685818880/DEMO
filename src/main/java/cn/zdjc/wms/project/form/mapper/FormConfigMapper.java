package cn.zdjc.wms.project.form.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.zdjc.wms.project.form.model.entity.FormConfigEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FormConfigMapper extends BaseMapper<FormConfigEntity> {
    FormConfigEntity selectByCode(String code);
    int checkCodeExists(String code);
}
