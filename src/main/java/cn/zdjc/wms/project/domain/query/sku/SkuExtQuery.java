package cn.zdjc.wms.project.domain.query.sku;

import cn.zdjc.warehouse.ExQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SkuExtQuery extends ExQuery {
    private String skuCode;

    private String skuName;

    private String region;

    private String lastModifyBy;


}
