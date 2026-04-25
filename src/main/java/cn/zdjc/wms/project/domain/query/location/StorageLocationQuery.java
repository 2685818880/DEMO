package cn.zdjc.wms.project.domain.query.location;

import cn.zdjc.warehouse.ExQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StorageLocationQuery extends ExQuery {
    private String houseCode;

    private Integer xPos;

    private String locNo;
}
