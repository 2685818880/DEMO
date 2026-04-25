package cn.zdjc.wms.project.form.service;

import cn.zdjc.wms.project.form.model.entity.FormDataEntity;
import java.util.List;
import java.util.Optional;

public interface FormDataService {
    FormDataEntity submitFormData(FormDataEntity entity);
    Optional<FormDataEntity> getFormDataById(Long id);
    List<FormDataEntity> listFormData(String formCode);
    boolean updateFormDataStatus(Long id, String status);
    boolean deleteFormData(Long id);
    boolean validateFormData(String formCode, String formDataJson);
}
