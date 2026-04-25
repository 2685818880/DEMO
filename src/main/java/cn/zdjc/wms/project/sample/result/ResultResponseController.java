package cn.zdjc.wms.project.sample.result;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foreris.eris.common.result.ObjectResult;
import com.foreris.eris.common.result.ResponseWrap;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 
 * @version 3
 * @author y
 * @create.tag 2021年1月20日
 */
@RestController
@RequestMapping("/sample/unification-response")
@Tag(name = "test统一返回")
@ResponseWrap
@Slf4j
public class ResultResponseController {

    /**
     * 正常返回对象
     */
    @GetMapping("/value")
    @Operation(summary = "value", responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = { @Content(schema = @Schema(implementation = ObjectResult.class)) }) })
    public Integer value() {
        log.debug("test for wrap object...");
        return 666;
    }

    @GetMapping("/value2")
    @Operation(description = "value2")
    public Integer value2() {
        log.debug("test for wrap object...");
        return 666;
    }

}
