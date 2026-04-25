package cn.zdjc.wms.project.domain.dto.pick;

import lombok.Data;

import java.util.List;

@Data
public class PickFindInfoDto {

    /**
     * 拣选站台
     */
    private String pickStation;

    /**
     * 容器号
     */
    private String containerCode;

    /**
     * 单据号
     */
    private String businessNo;

    /**
     * 仓库号
     */
    private String houseCode;

    /**
     * 租户号
     */
    private String customerCode;

    /**
     * 单据号
     */
    private List<String> businessNoList;
}
