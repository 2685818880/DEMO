package cn.zdjc.wms.project.domain.entity.transport;

import cn.zdjc.bm.transjob.domain.entity.DispatchJob;
import com.foeris.y.common.bean.BeanAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * Description
 *
 * @version: 1.0.0
 * @author: liuyk
 * @create.date: 2020年05月20日 13:51
 */
@EqualsAndHashCode(callSuper = true)
@Data
@BeanAlias("wms_dispatch_job")
public class DispatchJobExtEntity extends DispatchJob {

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
     * 序列码
     */
    @BeanAlias("seq_code")
    private String seqCode;

    /**
     * 序列码总数
     */
    @BeanAlias("seq_count")
    private String seqCount;

    /**
     * 工作模式
     */
    @BeanAlias("workstation_mode")
    private String workstationMode;

    /**
     * 双工标识
     */
    @BeanAlias("duplex")
    private Integer duplex;

    /**
     * 上次任务号,记录兰剑申请时的任务号，需要在下发任务时携带
     */
    @BeanAlias("last_task_no")
    private Integer lastTaskNo;

    @BeanAlias("device_code")
    private String deviceCode;

    @BeanAlias("apply_task_device")
    private String applyTaskDevice;
}
