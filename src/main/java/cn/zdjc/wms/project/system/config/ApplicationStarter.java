package cn.zdjc.wms.project.system.config;

import org.springframework.context.annotation.Import;

import com.foreris.eris.frame.system.SystemStarter;

/**
 * ApplicationStarter 为主的模式
 * 可因为idea不认识 这个项目为spring boot项目
 * 所以放到application上
 * 
 * @author y
 * @version 3
 * @create.tag 2020年12月12日
 */
@Import({
        SystemStarter.class,
        ApplicationScanComponent.class
})
public class ApplicationStarter {

}
