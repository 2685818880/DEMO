package cn.zdjc.wms.project.form.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FormDataSource {
    String value();
}
