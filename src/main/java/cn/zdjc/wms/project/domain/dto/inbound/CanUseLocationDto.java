package cn.zdjc.wms.project.domain.dto.inbound;

import lombok.Data;

import java.util.List;

@Data
public class CanUseLocationDto {

    /**
     * 仓库号
     */
    private String houseCode;

    /**
     * 巷道id
     */
    private String roadwayId;

    /**
     * 层号
     */
    private Integer layer;

    /**
     * 列号
     */
    private Integer col;

    /**
     * 不属于列号
     */
    private Integer notCol;

    /**
     * 区域id 集合
     */
    private List<String> zoneIdList;

    /**
     * 不属于的库位集合
     */
    private List<String> notLocationIdList;

}
