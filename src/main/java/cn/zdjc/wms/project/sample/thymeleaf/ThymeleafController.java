package cn.zdjc.wms.project.sample.thymeleaf;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

/**
 * ThymeleafController
 * 
 * @version 3
 * @author y
 * @create.tag 2020年10月15日
 */
@Controller
@RequestMapping("/sample/thymeleaf")
@Slf4j
public class ThymeleafController {

    @GetMapping("/message")
    public String message(Model model) {
        log.debug("thymeleaf 测试");
        model.addAttribute("msg", "message");
        return "/sample/message";
    }

}
