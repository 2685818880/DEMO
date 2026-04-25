package cn.zdjc.wms.project.system.config;

import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;

import com.foreris.eris.common.spring.ComponentExclude;
import com.foreris.eris.common.spring.SystemBeanNameGenerator;

import org.springframework.context.annotation.FilterType;
@ComponentScan(basePackages = { ApplicationConfig.PACKAGE },
        nameGenerator = SystemBeanNameGenerator.class,
        excludeFilters = {
                @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
                @Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class),
                @Filter(type = FilterType.ANNOTATION, classes = ComponentExclude.class)
        })
public class ApplicationScanComponent {

}
