package cn.zdjc.wms.project.sample.swagger;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.foreris.eris.common.result.ResponseResult;
import com.foreris.eris.common.result.ResponseWrap;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 演示用的swagger文档说明
 * 
 * 返回结果如果是泛型目前无相关解决方案
 * 如果有特殊要求则定制指定的类型，仅作为文档用
 * 
 * @version 3
 * @author y
 * @create.tag 2021年1月23日
 */
@Controller
@ResponseWrap
@RequestMapping("/sample/swagger")
@Tag(name = "swagger案例")
public class SwaggerController {

    @GetMapping("/")
    @Operation(description = "首页")
    @ResponseBody
    public String index() {
        return "this is a demo";
    }

    @GetMapping("/get/{name}")
    @Operation(description = "获取用户名称")
    @ResponseBody
    @ApiResponses({ //
            @ApiResponse(responseCode = "200", description = "OK",
                    content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResponseResult.class)) })
    })
    public String getName(@PathVariable String name) {//@ApiParam(value = "用户名", required = true)
        return "name: " + name;
    }

    @PostMapping("/pojo")
    @ResponseBody
    @Operation(summary = "获取用户信息", description = "这是一个很长的注释<br>" + "换行继续描述")
    public SwaggerPojoView getPojo(@RequestBody SwaggerPojoQuery query) {//@ApiParam(value = "查询pojo") 
        return SwaggerPojoView.builder().username(query.getUsername()).password(query.getPassword()).build();
    }

}
