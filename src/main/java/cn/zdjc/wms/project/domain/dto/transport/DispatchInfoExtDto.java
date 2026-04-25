package cn.zdjc.wms.project.domain.dto.transport;

import cn.zdjc.bm.transjob.domain.dto.DispatchInfoDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description
 *
 * @version: 1.0.0
 * @author: liuyk
 * @create.date: 2020年05月20日 19:15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DispatchInfoExtDto extends DispatchInfoDto {
    /**
     * 源库位号
     */
    private String sourceLocation;

    /**
     * 最大深度值
     */
    private String maxDepth;

    /**
     * 当前深度值
     */
    private String nowDepth;
    /**
     * 分配目标位
     */
    private String toDevice;

    /**
     * 优先级
     */
    private Integer task_level;
}
