// ToolingContainerInfoDto.java
package cn.zdjc.wms.project.domain.dto.inbound;

import lombok.Data;
import java.util.List;

@Data
public class ToolingContainerInfoDto {


    private List<ToolingContainerDto> containerList;
}