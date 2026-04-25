package cn.zdjc.wms.project.sample.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @version 3
 * @author y
 * @create.tag 2020年4月1日
 */
@Controller
@RequestMapping("/sample/spring")
@Slf4j
public class SpringController {

    @Autowired
    ApplicationContext applicationContext;

    /**
     * 获取所有的请求
     * 类
     * 方法
     * 地址
     */
    @GetMapping("/url")
    @ResponseBody
    public List<String> getAllUrl() {
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
        List<String> urls = new ArrayList<>();
        for (RequestMappingInfo info : map.keySet()) {

            System.out.println("----    ----");
            System.out.println("info: " + info);
            HandlerMethod handlerMethod = map.get(info);
            System.out.println("handler: " + handlerMethod);
            System.out.println("type: " + handlerMethod.getBeanType());
            System.out.println("method: " + handlerMethod.getMethod());
            Set<String> patterns = info.getPathPatternsCondition().getPatternValues();
            for (String url : patterns) {
                System.out.println("url: " + url);
                urls.add(url);
            }
        }
        return urls;
    }

    @GetMapping("/bean")
    public String bean(Model model) {
        String beans = getAllBeanName(applicationContext);
        model.addAttribute("out", beans);
        return "/sample/bean";
    }

    /**
     * 测试扫描到的所有对象
     */
    public static String getAllBeanName(ApplicationContext applicationContext) {
        StringBuilder sb = new StringBuilder();
        sb.append("Spring容器中的实例有：\n");
        for (String e : applicationContext.getBeanDefinitionNames()) {
            sb.append(e + "\n");//\n
        }
        sb.append("容器中的对象数量：" + applicationContext.getBeanDefinitionCount() + "\n");
        return sb.toString();
    }

    public static void showCustomerBeanName(ApplicationContext applicationContext) {
        log.debug(getCustomerBeanName(applicationContext));
    }

    /**
     * 测试用户自定义的对象
     */
    public static String getCustomerBeanName(ApplicationContext applicationContext) {
        StringBuilder sb = new StringBuilder();
        sb.append("Spring容器中的实例有：\n");
        int i = 0;
        for (String e : applicationContext.getBeanDefinitionNames()) {
            if (e.startsWith("org.springframework")) {
                continue;
            }
            i++;
            sb.append(e + "\n");
        }
        sb.append("容器中的自定义对象数量：" + i + "/" + applicationContext.getBeanDefinitionCount() + "\n");
        return sb.toString();
    }

}
