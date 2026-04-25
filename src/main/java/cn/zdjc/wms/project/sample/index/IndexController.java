package cn.zdjc.wms.project.sample.index;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.foreris.eris.common.result.ResponseNoWrap;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 
 * @version 3
 * @author y
 * @create.tag 2021年4月26日
 */
@Controller
@RequestMapping("/sample")
@Slf4j
@Tag(name = "index")
public class IndexController {

    @GetMapping("/hello")
    @ResponseBody
    @Operation(description = "hello 中文")
    public String hello() {
        log.debug("hello 中文");
        return "hello 中文";
    }

    @GetMapping("/hello2")
    @ResponseBody
    @Operation(description = "hello2 中文")
    @ResponseNoWrap
    public String hello2() {
        log.debug("hello2 中文");
        return "hello2 中文";
    }

}
