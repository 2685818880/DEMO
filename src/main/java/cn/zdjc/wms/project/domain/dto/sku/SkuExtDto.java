package cn.zdjc.wms.project.domain.dto.sku;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.foeris.y.common.bean.BeanAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkuExtDto {

    @BeanAlias("sku_code")
    @ExcelProperty(value = "物料编码")
    private String skuCode;

    @BeanAlias("sku_name")
    @ExcelProperty(value = "物料名称")
    private String skuName;

    @BeanAlias("unit")
    @ExcelProperty(value = "计量单位")
    private String unit;


//    @ExcelProperty(value = "分区")
//    private String region;

    @BeanAlias("last_modify_by")
    @ExcelIgnore
    private String lastModifyBy;
}
