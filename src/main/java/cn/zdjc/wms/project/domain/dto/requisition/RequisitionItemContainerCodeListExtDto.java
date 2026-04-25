package cn.zdjc.wms.project.domain.dto.requisition;

import cn.zdjc.wms.project.domain.dto.pick.PickItemExtDto;
import com.alibaba.excel.annotation.ExcelProperty;
import com.foeris.y.common.bean.BeanAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequisitionItemContainerCodeListExtDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String workstationCode;
    /**
     * 拣选容器/任务列表
     */
    private List<RequisitionItemContainerCodeExtDto> pickContainerList;



}