package cn.zdjc.wms.project.system.config;

import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.foreris.eris.frame.result.SystemExceptionConfiguration;

/**
 * GlobalExceptionAdvice
 * 缩小作用范围
 * <p>
 * 统一异常处理
 * <p>
 * Throwable考虑要不要，这个范围有点大
 *
 * @author y
 * @version 3
 * @create.tag 2020年10月30日
 */
@RestControllerAdvice(basePackages = ApplicationConfig.PACKAGE)
public class ApplicationExceptionConfiguration extends SystemExceptionConfiguration {

}
