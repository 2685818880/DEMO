package cn.zdjc.wms.project.system.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

import com.foreris.eris.common.spring.SystemBeanNameGenerator;

/**
 * mybatis 需要configuration扫描
 * 
 * @version 3
 * @author y
 * @create.tag 2021年1月30日
 */
@Configuration
@MapperScan(basePackages = ApplicationConfig.PACKAGE, nameGenerator = SystemBeanNameGenerator.class, annotationClass = Mapper.class)
public class ApplicationScanMybatis {

}
