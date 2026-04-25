package cn.zdjc.wms.project.system.config;

import org.springframework.web.bind.annotation.ControllerAdvice;

import com.foreris.eris.frame.result.SystemResponseConfiguration;

/**
 * 统一返回处理
 * 
 * 提供了注解的方式
 * 
 * ResponseBodyAdvice技术来实现response的统一格式处理
 * 一般用来处理返回值和响应体，做数据的加密和解密，签名等等。
 */
@ControllerAdvice(basePackages = ApplicationConfig.PACKAGE)
public class ApplicationResponseConfiguration extends SystemResponseConfiguration {

}
