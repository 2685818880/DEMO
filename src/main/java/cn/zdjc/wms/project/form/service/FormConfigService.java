package cn.zdjc.wms.project.form.service;

import cn.zdjc.wms.project.form.model.entity.FormConfigEntity;
import java.util.List;
import java.util.Optional;

public interface FormConfigService {
    FormConfigEntity saveFormConfig(FormConfigEntity entity);
    Optional<FormConfigEntity> getFormConfigById(Long id);
    Optional<FormConfigEntity> getFormConfigByCode(String code);
    List<FormConfigEntity> listFormConfigs();
    boolean updateFormConfigStatus(String code, Integer status);
    boolean deleteFormConfig(String code);
    boolean validateFormJson(String configJson);
}
