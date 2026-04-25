package cn.zdjc.wms.project.system.customer;

import cn.zdjc.warehouse.configs.WarehouseMybatisScan;
import cn.zdjc.wms.common.configs.CommonMybatisScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * mybatis 需要configuration扫描
 * 
 * @version 3
 * @author y
 * @create.tag 2021年1月30日
 */

@Configuration
@Import({WarehouseMybatisScan.class, CommonMybatisScan.class})
public class ZdjcScanMybatis {

}
