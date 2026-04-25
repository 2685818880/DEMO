package cn.zdjc.wms.project;

import com.foreris.eris.frame.database.flyway.FlywayModule;
import org.springframework.stereotype.Component;

/**
 * 描述信息
 *
 * @version: 1.0.0
 * @author: liuyk
 * @create.date: 2022年03月17日 14:04
 */
@Component
public class ProjectFlywayModule implements FlywayModule {
    @Override
    public String code() {
        return "project";
    }

    @Override
    public int order() {
        return 4;
    }
}
