package cn.zdjc.wms.project.domain.dto.pick;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.Data;


@Data
public class WorkstationPickInfoDto {

    /**
     * 工作站编号
     */
    private String workstationCode;

    /**
     * 容器号
     */
    private String containerCode;


    public void checkData() {
        if (StringUtils.isEmpty(workstationCode)) {
            throw new IllegalArgumentException("工作站编号不能为空!");
        }
        if (StringUtils.isEmpty(containerCode)) {
            throw new IllegalArgumentException("容器号不能为空!");
        }
    }


}
