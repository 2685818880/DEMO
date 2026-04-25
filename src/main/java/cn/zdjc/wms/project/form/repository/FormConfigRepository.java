package cn.zdjc.wms.project.form.repository;

import cn.zdjc.wms.project.form.mapper.FormConfigMapper;
import cn.zdjc.wms.project.form.model.entity.FormConfigEntity;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;

@Repository
public class FormConfigRepository {

    @Autowired
    private FormConfigMapper formConfigMapper;

    public FormConfigEntity save(FormConfigEntity entity) {
        if (entity.getId() == null) {
            formConfigMapper.insert(entity);
        } else {
            formConfigMapper.updateById(entity);
        }
        return entity;
    }

    public Optional<FormConfigEntity> findById(Long id) {
        return Optional.ofNullable(formConfigMapper.selectById(id));
    }

    public Optional<FormConfigEntity> findByCode(String code) {
        return Optional.ofNullable(formConfigMapper.selectByCode(code));
    }

    public boolean existsByCode(String code) {
        return formConfigMapper.checkCodeExists(code) > 0;
    }

    public int deleteById(Long id) {
        return formConfigMapper.deleteById(id);
    }
}
