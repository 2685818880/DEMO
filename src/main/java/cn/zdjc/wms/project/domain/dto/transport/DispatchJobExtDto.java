package cn.zdjc.wms.project.domain.dto.transport;

import cn.zdjc.bm.transjob.domain.dto.DispatchJobDto;
import com.foeris.y.common.bean.BeanAlias;
import com.foeris.y.common.bean.BeanIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * Description
 *
 * @version: 1.0.0
 * @author: liuyk
 * @create.date: 2020年05月20日 19:47
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DispatchJobExtDto extends DispatchJobDto {

    private static final long serialVersionUID = 1L;

    /**
     * 最大深度值
     */
    private String maxDepth;

    /**
     * 当前深度值
     */
    private String nowDepth;

    /**
     * 序列码
     */
    private String seqCode;

    /**
     * 序列码总数
     */
    private String seqCount;

    /**
     * location_allow_depth
     */
    @BeanIgnore
    private String allow_depth;

    /**
     * 工作模式
     */
    private String workstationMode;

    /**
     * 双工标识
     */
    private Integer duplex;

    /**
     * 上次任务号,记录兰剑申请时的任务号，需要在下发任务时携带
     */
    @BeanAlias("last_task_no")
    private String lastTaskNo;

    @BeanAlias("apply_task_device")
    private String applyTaskDevice;

    /**
     * 执行设备
     */
    private String deviceCode;
}
