package cn.zdjc.wms.project.form.service.impl;

import cn.zdjc.wms.project.form.mapper.FormDataMapper;
import cn.zdjc.wms.project.form.model.entity.FormConfigEntity;
import cn.zdjc.wms.project.form.model.entity.FormDataEntity;
import cn.zdjc.wms.project.form.repository.FormDataRepository;
import cn.zdjc.wms.project.form.service.DynamicTableService;
import cn.zdjc.wms.project.form.service.FormConfigService;
import cn.zdjc.wms.project.form.service.FormDataService;
import com.alibaba.fastjson2.JSONValidator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.Optional;

@Service
public class FormDataServiceImpl implements FormDataService {

    @Autowired
    private FormDataRepository formDataRepository;

    @Autowired
    private FormDataMapper formDataMapper;

    @Autowired
    private DynamicTableService dynamicTableService;

    @Autowired
    private FormConfigService formConfigService;

    @Override
    public FormDataEntity submitFormData(FormDataEntity entity) {
        if (!StringUtils.hasText(entity.getFormCode())) {
            throw new IllegalArgumentException("表单编码不能为空");
        }
        if (!validateFormData(entity.getFormCode(), entity.getFormDataJson())) {
            throw new IllegalArgumentException("表单数据验证失败");
        }
        // 自动确保动态表存在
        Optional<FormConfigEntity> config = formConfigService.getFormConfigByCode(entity.getFormCode());
        if (config.isPresent()) {
            dynamicTableService.createDynamicTable(config.get());
        }
        return formDataRepository.save(entity);
    }

    @Override
    public Optional<FormDataEntity> getFormDataById(Long id) {
        return formDataRepository.findById(id);
    }

    @Override
    public List<FormDataEntity> listFormData(String formCode) {
        QueryWrapper<FormDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("form_code", formCode)
                    .orderByDesc("created_time");
        return formDataMapper.selectList(queryWrapper);
    }

    @Override
    public boolean updateFormDataStatus(Long id, String status) {
        Optional<FormDataEntity> optional = formDataRepository.findById(id);
        if (optional.isPresent()) {
            FormDataEntity entity = optional.get();
            entity.setDataStatus(status);
            formDataRepository.save(entity);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteFormData(Long id) {
        return formDataRepository.deleteById(id) > 0;
    }

    @Override
    public boolean validateFormData(String formCode, String formDataJson) {
        if (!StringUtils.hasText(formDataJson)) {
            return false;
        }
        try {
            JSONValidator validator = JSONValidator.from(formDataJson);
            return validator.validate();
        } catch (Exception e) {
            return false;
        }
    }
}
