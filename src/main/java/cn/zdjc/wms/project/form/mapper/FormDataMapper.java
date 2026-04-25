package cn.zdjc.wms.project.form.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.zdjc.wms.project.form.model.entity.FormDataEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FormDataMapper extends BaseMapper<FormDataEntity> {
    FormDataEntity selectByFormCodeAndBusinessKey(String formCode, String businessKey);
    int countByFormCode(String formCode);
}
