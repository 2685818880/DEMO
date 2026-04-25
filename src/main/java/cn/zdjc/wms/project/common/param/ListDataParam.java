package cn.zdjc.wms.project.common.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zhao
 * @date 11/22/22 1:20 PM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListDataParam<T> {

    private List<T> data;

}
