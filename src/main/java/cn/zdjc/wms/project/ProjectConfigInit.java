package cn.zdjc.wms.project;

import cn.zdjc.platform.system.manage.dictionary.DictionaryAutoInitial;
import org.springframework.stereotype.Component;

/**
 * TODO
 *
 * @Author frj
 * @Date 2022/8/10 15:08
 */
@Component
public class ProjectConfigInit implements DictionaryAutoInitial {

    @Override
    public Class<?> dictionaryClass() {
        return ProjectConfig.class;
    }
}
