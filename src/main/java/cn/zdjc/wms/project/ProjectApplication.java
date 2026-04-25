package cn.zdjc.wms.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

import com.foreris.eris.common.util.DevelopUtil;
import cn.zdjc.wms.project.system.config.ApplicationConfig;
import cn.zdjc.wms.project.system.config.ApplicationStarter;
import cn.zdjc.wms.project.system.customer.ZdjcStarter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * @author y
 * @version 2022-05-13
 */
@Slf4j
@SpringBootConfiguration
@EnableAutoConfiguration
@Import({
    ApplicationStarter.class,
    ZdjcStarter.class
    //SpringWebSocketConfigurator.class
})

public class ProjectApplication {

    public static void initial(String[] args) {
        System.setProperty("druid.mysql.usePingMethod","false");
        /*
         * notes
         * 
         * DevelopUtil.configPath("D:\\config\\docker");
         * DevelopUtil.configPath("D:\\config\\eriy");
         * -Dsystem_config=D:\config\eriy
         */

        /*
         * notes
         * 
         * java.exe --add-modules=ALL-SYSTEM --add-opens=java.base/java.lang=ALL-UNNAMED -Dspring.output.ansi.enabled=always
         */

        /*
         * notes
         * 
         * 环境
         * -Dsystem_profiles=dev
         */
        DevelopUtil.profiles("dev");

        /*
         * notes
         * 
         * 注册中心设置
         * DevelopUtil.register("http://localhost:20079/eureka/");
         * -Dsystem_register=true -Dsystem_eureka=http://localhost:20079/eureka/
         */

        /*
         * notes
         * 
         * 应用名 applicationName，和应用访问路径 contextPath
         * -Dsystem_name=app -Dserver_port=80 -Dserver_path=/
         */
        DevelopUtil.name(ApplicationConfig.NAME);
        DevelopUtil.server("8080", "/app");

        /*
         * notes
         * 
         * 打印和输出
         */
        DevelopUtil.run();
        DevelopUtil.log(log, args);

    }

    /**
     * 项目的启动入口
     * 
     * jdk 11配置：--add-modules=ALL-SYSTEM --add-opens=java.base/java.lang=ALL-UNNAMED -Djava.security.egd=file:/dev/./urandom
     * --add-modules=ALL-SYSTEM --add-opens=java.base/java.lang=ALL-UNNAMED -Dsystem_config=D:\config\eriy
     */
    public static void main(String[] args) {

        log.debug("-------- app main --------");

        initial(args);

        ConfigurableApplicationContext ctx = SpringApplication.run(ProjectApplication.class, args);

        // 诊断：检查该类是否在 classpath 中
        try {
            Class<?> clazz = Class.forName("cn.zdjc.wms.project.common.socket.ApplicationStartupListener");
            System.out.println("✅ Class found in classpath: " + clazz.getName());
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Class NOT found in classpath!");
        }

        // 诊断：检查 Bean 是否注册
        String[] beans = ctx.getBeanNamesForType(
                ApplicationListener.class, true, true
        );
        System.out.println("🔍 所有 ApplicationListener Beans: " + Arrays.toString(beans));

        // 验证 Bean 是否被注册
        if (ctx.containsBean("applicationStartupListener")) {
            System.out.println("✅ ApplicationStartupListener Bean 已注册");
        } else {
            System.out.println("❌ ApplicationStartupListener Bean 未注册");
        }

        // 验证事件监听器
        ApplicationListener<ApplicationReadyEvent>[] listeners =
                ctx.getBeansOfType(ApplicationListener.class).values()
                        .stream()
                        .filter(l -> l instanceof ApplicationListener)
                        .toArray(ApplicationListener[]::new);
        System.out.println("🔍 已注册 ApplicationListener 数量: " + listeners.length);
        System.out.println("✅ Spring Boot 应用启动成功！");
        System.out.println("🌐 访问地址: http://localhost:8080");
        System.out.println("🔌 WebSocket 端点: ws://localhost:8080/app/websocket/{workstationCode}");
    }

}
