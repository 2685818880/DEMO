package cn.zdjc.wms.project.domain.entity.location;

import cn.zdjc.warehouse.definition.domain.entity.StorageLocation;
import com.foeris.y.common.bean.BeanAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@BeanAlias("wms_storage_location")
public class StorageLocationExtEntity extends StorageLocation {

}
