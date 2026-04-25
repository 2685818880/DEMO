package cn.zdjc.wms.project.domain.entity.transport;

import cn.zdjc.bm.transjob.domain.entity.DispatchInfo;
import com.foeris.y.common.bean.BeanAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description
 *
 * @version: 1.0.0
 * @author: liuyk
 * @create.date: 2020年05月20日 13:50
 */
@EqualsAndHashCode(callSuper = true)
@Data
@BeanAlias("wms_dispatch_info")
public class DispatchInfoExtEntity extends DispatchInfo {
    /**
     * 源库位号
     */
    @BeanAlias("source_location")
    private String sourceLocation;

    /**
     * 最大深度值
     */
    @BeanAlias("max_depth")
    private String maxDepth;

    /**
     * 当前深度值
     */
    @BeanAlias("now_depth")
    private String nowDepth;

    /**
     * 分配目标位
     */
    @BeanAlias("to_device")
    private String toDevice;

    /**
     * 优先级
     */
    private Integer task_level;
}
