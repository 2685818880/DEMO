package cn.zdjc.wms.project.domain.query.asn;

import cn.zdjc.warehouse.ExQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AsnItemReportQuery extends ExQuery {
    private String formType;
}
