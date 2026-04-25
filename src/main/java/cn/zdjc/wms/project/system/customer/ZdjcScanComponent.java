package cn.zdjc.wms.project.system.customer;

import cn.zdjc.platform.system.ZdjcPlatformConfig;
import com.foreris.eris.common.spring.ComponentExclude;
import com.foreris.eris.common.spring.SystemBeanNameGenerator;
import com.foreris.eris.frame.y.ZdjcFrameConfig;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;

@ComponentScan(basePackages = { ZdjcFrameConfig.PACKAGE, ZdjcPlatformConfig.PACKAGE, "com.wxzd", "cn.zdjc" },
        nameGenerator = SystemBeanNameGenerator.class,
        excludeFilters = {
                @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
                @Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class),
                @Filter(type = FilterType.ANNOTATION, classes = ComponentExclude.class)
        })
public class ZdjcScanComponent {

}
