package cn.zdjc.wms.project.form.service.impl;

import cn.zdjc.wms.project.form.mapper.FormConfigMapper;
import cn.zdjc.wms.project.form.model.entity.FormConfigEntity;
import cn.zdjc.wms.project.form.repository.FormConfigRepository;
import cn.zdjc.wms.project.form.service.FormConfigService;
import com.alibaba.fastjson2.JSONValidator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.Optional;

@Service
public class FormConfigServiceImpl implements FormConfigService {

    @Autowired
    private FormConfigRepository formConfigRepository;

    @Autowired
    private FormConfigMapper formConfigMapper;

    @Override
    public FormConfigEntity saveFormConfig(FormConfigEntity entity) {
        if (!StringUtils.hasText(entity.getCode())) {
            throw new IllegalArgumentException("表单编码不能为空");
        }
        if (!StringUtils.hasText(entity.getName())) {
            throw new IllegalArgumentException("表单名称不能为空");
        }
        if (!validateFormJson(entity.getConfigJson())) {
            throw new IllegalArgumentException("表单JSON配置格式无效");
        }
        if (formConfigRepository.existsByCode(entity.getCode())) {
            throw new IllegalArgumentException("表单编码已存在: " + entity.getCode());
        }
        return formConfigRepository.save(entity);
    }

    @Override
    public Optional<FormConfigEntity> getFormConfigById(Long id) {
        return formConfigRepository.findById(id);
    }

    @Override
    public Optional<FormConfigEntity> getFormConfigByCode(String code) {
        return formConfigRepository.findByCode(code);
    }

    @Override
    public List<FormConfigEntity> listFormConfigs() {
        QueryWrapper<FormConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                    .orderByDesc("created_time");
        return formConfigMapper.selectList(queryWrapper);
    }

    @Override
    public boolean updateFormConfigStatus(String code, Integer status) {
        Optional<FormConfigEntity> optional = formConfigRepository.findByCode(code);
        if (optional.isPresent()) {
            FormConfigEntity entity = optional.get();
            entity.setStatus(status);
            formConfigRepository.save(entity);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteFormConfig(String code) {
        Optional<FormConfigEntity> optional = formConfigRepository.findByCode(code);
        if (optional.isPresent()) {
            formConfigRepository.deleteById(optional.get().getId());
            return true;
        }
        return false;
    }

    @Override
    public boolean validateFormJson(String configJson) {
        if (!StringUtils.hasText(configJson)) {
            return false;
        }
        try {
            JSONValidator validator = JSONValidator.from(configJson);
            return validator.validate();
        } catch (Exception e) {
            return false;
        }
    }
}
