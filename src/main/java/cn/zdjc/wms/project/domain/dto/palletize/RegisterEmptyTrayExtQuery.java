package cn.zdjc.wms.project.domain.dto.palletize;

import lombok.Data;

/**
 * 描述信息
 *
 * @version: 1.0.0
 * @author: liuyk
 * @create.date: 2021年04月01日 09:03
 */
@Data
public class RegisterEmptyTrayExtQuery {
    /**
     * 仓库号
     */
    private String houseCode;
    /**
     * 来源
     */
    private String fromPos;

    /**
     * 托盘号
     */
    private String containerCode;

    /**
     * 码垛数量
     */
    private Integer emptyQty;

}
