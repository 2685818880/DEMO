package cn.zdjc.wms.project.form.repository;

import cn.zdjc.wms.project.form.mapper.FormDataMapper;
import cn.zdjc.wms.project.form.model.entity.FormDataEntity;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;

@Repository
public class FormDataRepository {

    @Autowired
    private FormDataMapper formDataMapper;

    public FormDataEntity save(FormDataEntity entity) {
        if (entity.getId() == null) {
            formDataMapper.insert(entity);
        } else {
            formDataMapper.updateById(entity);
        }
        return entity;
    }

    public Optional<FormDataEntity> findById(Long id) {
        return Optional.ofNullable(formDataMapper.selectById(id));
    }

    public Optional<FormDataEntity> findByFormCodeAndBusinessKey(String formCode, String businessKey) {
        return Optional.ofNullable(formDataMapper.selectByFormCodeAndBusinessKey(formCode, businessKey));
    }

    public int countByFormCode(String formCode) {
        return formDataMapper.countByFormCode(formCode);
    }

    public int deleteById(Long id) {
        return formDataMapper.deleteById(id);
    }
}
