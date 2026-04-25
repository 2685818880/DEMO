package cn.zdjc.wms.project.system.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.extern.slf4j.Slf4j;

/**
 * http://localhost/doc
 * 
 * http://localhost/doc.html
 * 
 * http://localhost/swagger-ui.html
 * 
 * 在完成上述配置之后，其实就已经可以产生帮助文档了，但是这样的文档主要针对请求本身，而描述主要来源于函数等命名产生。
 * 对用户体验不好，我们通常需要自己增加一些说明来丰富文档内容。如果：
 * 加入
 * 
 * @ApiIgnore
 *            忽略暴露的 api
 * 
 * @ApiOperation(value = "查找", notes = "根据用户 ID 查找用户")
 *                     添加说明
 * 
 * 
 *                     其他注解：
 * @Api ：用在类上，说明该类的作用
 * @ApiImplicitParams ：用在方法上包含一组参数说明
 * @ApiResponses ：用于表示一组响应
 *               完成上述之后，启动springboot程序，访问：http://localhost:8080/swagger-ui.html
 * 
 * 
 * @ApiOperation() 用于方法；表示一个http请求的操作
 *                 value用于方法描述
 *                 notes用于提示内容
 *                 tags可以重新分组（视情况而用）
 * @ApiParam() 用于方法，参数，字段说明；表示对参数的添加元数据（说明或是否必填等）
 *             name–参数名
 *             value–参数说明
 *             required–是否必填
 * 
 * @ApiModel()用于类 ；表示对类进行说明，用于参数用实体类接收
 *                value–表示对象名
 *                description–描述
 *                都可省略
 * @ApiModelProperty()用于方法，字段； 表示对model属性的说明或者数据操作更改
 *                             value–字段说明
 *                             name–重写属性名字
 *                             dataType–重写属性类型
 *                             required–是否必填
 *                             example–举例说明
 *                             hidden–隐藏
 * 
 * @ApiIgnore()用于类或者方法上，可以不被swagger显示在页面上
 *                                        比较简单, 这里不做举例
 * 
 * @ApiImplicitParam() 用于方法
 *                     表示单独的请求参数
 * @ApiImplicitParams() 用于方法，包含多个 @ApiImplicitParam
 *                      name–参数ming
 *                      value–参数说明
 *                      dataType–数据类型
 *                      paramType–参数类型
 *                      example–举例说明
 * @version 3
 * @author y
 * @create.tag 2020年10月27日
 */
@Profile("!prod")
@Configuration
@Slf4j
public class ApplicationScanSwagger {

    public ApplicationScanSwagger() {
        log.debug("constructor swagger application " + ApplicationConfig.NAME + " config");
    }

  @Bean
  public GroupedOpenApi publicApi() {
      return GroupedOpenApi.builder()
              .group("api")
              .packagesToScan(ApplicationConfig.PACKAGE)
              .build();
  }

}
