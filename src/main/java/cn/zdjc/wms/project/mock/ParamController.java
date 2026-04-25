package cn.zdjc.wms.project.mock;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foreris.eris.common.result.ResponseWrap;

import io.swagger.v3.oas.annotations.Operation;

/**
 * HelloWorldController
 * 
 * @version 3
 * @author y
 * @create.tag 2020年3月23日
 */
@ResponseWrap
@RestController
@RequestMapping("/test/param")
public class ParamController {

    @GetMapping("/hello-world")
    @Operation(description = "hello")
    public Object helloWorld() {
        return "hello world!";
    }

    @GetMapping("/hello-param")
    @Operation(description = "hello 参数")
    public Object helloParam(String name) {
        return "Hello " + name + "!";
    }

    @GetMapping("/hello-path/{name}")
    @Operation(description = "hello 路径")
    public Object helloPath(@PathVariable String name) {
        return "Hello " + name + "!";
    }

    @GetMapping("/hello-china")
    @Operation(description = "中文测试")
    public Object helloChina() {
        return "中国你好!";
    }

}
