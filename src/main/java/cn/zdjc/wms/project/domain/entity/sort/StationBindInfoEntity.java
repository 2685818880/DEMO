
package cn.zdjc.wms.project.domain.entity.sort;

import cn.zdjc.wms.project.common.enums.IfEnums;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.foeris.y.fairy.jdbc.ddd.Domain;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("wms_station_bind_info_record")
public class StationBindInfoEntity extends Domain {



    /**
     * 拣选站台
     */
    private String pickStation;

    /**
     * 站台配置id
     */
    private String sortConfigId;


    /**
     * 绑定的业务信息编号(已项目而定)
     */
    private String bindInfoCode;


    /**
     * 排序信息(以项目而定 可以是数字 可以是时间)
     */
    private String sortInfo;

    /**
     * 绑定状态
     * StationBindInfoStatusEnums
     */
    private String bindStatus;

    /**
     * 是否急单
     * IfEnums
     */
    private String isUrgent = IfEnums.NO.getCode();

}
