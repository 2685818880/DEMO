package cn.zdjc.wms.project.sample.swagger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "用户信息查询")
public class SwaggerPojoQuery {
    @Schema(description = "用户名", required = true)
    String username;
    @Schema(description = "密码", required = true)
    String password;

}
