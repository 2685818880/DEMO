package cn.zdjc.wms.project.form.aspect;

import cn.zdjc.wms.project.form.annotation.FormDataSource;
import cn.zdjc.wms.project.form.config.DynamicDataSource;
import cn.zdjc.wms.project.form.model.entity.FormConfigEntity;
import cn.zdjc.wms.project.form.service.FormConfigService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.Optional;

@Aspect
@Component
public class DataSourceAspect {

    @Autowired
    private FormConfigService formConfigService;

    @Before("@annotation(formDataSource)")
    public void switchDataSource(JoinPoint joinPoint, FormDataSource formDataSource) {
        String formCode = formDataSource.value();
        if (StringUtils.hasText(formCode)) {
            Optional<FormConfigEntity> config = formConfigService.getFormConfigByCode(formCode);
            if (config.isPresent() && StringUtils.hasText(config.get().getDbCode())) {
                DynamicDataSource.switchDataSource(config.get().getDbCode());
            } else {
                DynamicDataSource.resetToDefault();
            }
        } else {
            DynamicDataSource.resetToDefault();
        }
    }

    @After("@annotation(formDataSource)")
    public void clearDataSource(JoinPoint joinPoint, FormDataSource formDataSource) {
        DynamicDataSource.resetToDefault();
    }

    @Before("execution(* cn.zdjc.wms.project.form.service.FormDataService.*(..)) && args(formCode,..)")
    public void switchDataSourceByParam(JoinPoint joinPoint, String formCode) {
        if (StringUtils.hasText(formCode)) {
            Optional<FormConfigEntity> config = formConfigService.getFormConfigByCode(formCode);
            if (config.isPresent() && StringUtils.hasText(config.get().getDbCode())) {
                DynamicDataSource.switchDataSource(config.get().getDbCode());
            } else {
                DynamicDataSource.resetToDefault();
            }
        } else {
            DynamicDataSource.resetToDefault();
        }
    }

    @After("execution(* cn.zdjc.wms.project.form.service.FormDataService.*(..)) && args(formCode,..)")
    public void clearDataSourceByParam(JoinPoint joinPoint, String formCode) {
        DynamicDataSource.resetToDefault();
    }
}
