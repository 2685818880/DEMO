package cn.zdjc.wms.project.domain.dto.pick;

import com.foeris.y.common.tree.Dto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Map;

/**
 * 箱离开工作站请求参数
 *
 * @author chensor
 * @version 1.0.0
 * @since 2022-12-12
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OrderContainerLeaveDto extends Dto {

    /**
     * 系统编码
     */
    private String systemCode;

    /**
     * 库编号
     */
    private String houseCode;

    /**
     * 设备编号
     */
    private String deviceCode;

    /**
     * 工作站编号
     */
    @NotBlank(message = "工作站编号不能为空")
    private String workstationCode;

    /**
     * 订单箱子或者周转箱子容器号
     */
    @NotBlank(message = "容器号不能为空")
    private String containerCode;

    /**
     * 去向 (warehouse: 仓库, workshops: 车间)
     */
    @NotBlank(message = "目标位置不能为空")
    @Pattern(regexp = "warehouse|workshops", message = "目标位置取值必须为 warehouse 或 workshops")
    private String toPos;

    /**
     * 扩展字段
     */
    private Map<String, Object> parameters;
}