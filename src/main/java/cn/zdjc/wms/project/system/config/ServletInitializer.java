package cn.zdjc.wms.project.system.config;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import cn.zdjc.wms.project.ProjectApplication;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {

        log.debug("-------- web main --------");

        ProjectApplication.initial(null);

        return application.sources(ProjectApplication.class);
    }

}
