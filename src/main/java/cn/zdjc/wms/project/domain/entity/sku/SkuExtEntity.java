package cn.zdjc.wms.project.domain.entity.sku;

import cn.zdjc.warehouse.material.domain.entity.Sku;
import com.foeris.y.common.bean.BeanAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
@BeanAlias("wms_sku")
public class SkuExtEntity extends Sku {

//    /**
//     * 物料区域
//     */
//    private String region;
}

