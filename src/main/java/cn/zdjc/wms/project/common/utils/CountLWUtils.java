package cn.zdjc.wms.project.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * 装箱算法 只考虑长宽
 */
@Slf4j
public class CountLWUtils {

    /**
     * 朴素计算(不计算之前已经装了的体积) 大箱子能装多少个小箱子
     *
     * @param maxBox
     * @param minBox
     * @return 原理是：
     * nx < X
     * ny < Y
     * nz < Z
     * 交换 XYZ 比较顺序来计算各种方法最多能放多少个 取整乘积最大值就是可放置的最多个数（朴素算法）
     */
    public static Double toCount(CountBox maxBox, CountBox minBox) {
        double a = Math.floor(maxBox.getLength() / minBox.getLength()) *
                Math.floor(maxBox.getWidth() / minBox.getWidth()) *
                Math.floor(maxBox.getHeight() / minBox.getHeight());


        double b = Math.floor(maxBox.getLength() / minBox.getWidth()) *
                Math.floor(maxBox.getWidth() / minBox.getLength()) *
                Math.floor(maxBox.getHeight() / minBox.getHeight());

        List<Double> capacitiesList = new ArrayList<>();
        Stream.of(a, b).forEach(x -> capacitiesList.add(x));
        System.out.println(capacitiesList);
        return Collections.max(capacitiesList);
    }

    /**
     * 比较适用的现实的常规方法 计算前容器已完成了多少体积
     *
     * @param maxBox         大箱子
     * @param minBox         小盒子
     * @param occupyCapacity 已占用体积
     * @return 所能承载的小盒子
     */
    public static Double toCountComplex(CountBox maxBox, CountBox minBox, Double totalCapacity, Double occupyCapacity) {
        Double a = 0D, b = 0D, c = 0D;
        //剔除所占空间的部分将该部分之外的体积分成若干个小的部分的box 进行朴素计算
        Double surplusCapacity = totalCapacity - occupyCapacity;
        Double minNeedVolume = minBox.getVol();
        double minValue = Math.min(minBox.getLength(), minBox.getWidth());
        if (surplusCapacity > minNeedVolume) {
            double maxLength = Math.floor(surplusCapacity / (maxBox.getWidth() * maxBox.getHeight()));
            if (maxLength > minValue) {
                double lengthNew = maxLength;
                CountBox maxBoxNew = new CountBox(lengthNew, maxBox.getWidth(), maxBox.getHeight());
                a = toCount(maxBoxNew, minBox);
            }
            double maxWidth = Math.floor(surplusCapacity / (maxBox.getLength() * maxBox.getHeight()));
            if (maxWidth > minValue) {
                double widthNew = maxWidth;
                CountBox maxBoxNew = new CountBox(maxBox.getLength(), widthNew, maxBox.getHeight());
                b = toCount(maxBoxNew, minBox);
            }
        }
        return Math.max(a, Math.max(b, c));
    }
}
