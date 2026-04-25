package cn.zdjc.wms.project.common.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CountBox {

    private Double length;

    private Double width;

    private Double height;

    public Double getVol() {
        return (double) this.length * this.width * this.height;
    }
}
