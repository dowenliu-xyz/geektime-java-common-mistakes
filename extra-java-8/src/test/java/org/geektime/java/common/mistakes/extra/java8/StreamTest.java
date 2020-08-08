package org.geektime.java.common.mistakes.extra.java8;

import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 使用 {@link Stream} 简化代码示例
 * <p>create at 2020/8/8</p>
 *
 * @author liufl
 * @since 1.0
 */
public class StreamTest {
    /**
     * 一个可以转换为 Stream 操作的方法。主要工作：
     * <ol>
     *     <li>把整数列表转换为 {@link Point2D} 列表</li>
     *     <li>遍历 {@link Point2D} 列表过滤出 Y 轴 &gt;1 的对象</li>
     *     <li>计算 {@link Point2D} 点到原点的距离</li>
     *     <li>累加所有计算出的距离，并计算距离的平均值</li>
     * </ol>
     * @param ints 整数列表
     * @return 计算结果
     */
    private static double calc(List<Integer> ints) {
        // 临时中间集合
        List<Point2D> point2DList = new ArrayList<>();
        for (Integer i : ints) {
            point2DList.add(new Point2D.Double((double) i % 3, (double) i / 3));
        }
        // 临时变量，纯粹是为了获得最后结果需要的中间变量
        double total = 0;
        int count = 0;
        for (Point2D point2D : point2DList) {
            // 过滤
            if (point2D.getY() > 1) {
                // 算距离
                double distance = point2D.distance(0, 0);
                total += distance;
                count++;
            }
        }
        // 注意 count 可能为0
        return count > 0 ? total / count : 0;
    }

    @Test
    public void calcFunctionVsStream() {
        List<Integer> ints = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);
        double functionResult = calc(ints);
        double streamResult = ints.stream()
                .map(i -> new Point2D.Double((double) i % 3, (double) i / 3))
                .filter(point -> point.getY() > 1)
                .mapToDouble(point -> point.distance(0, 0))
                .average()
                .orElse(0);
        assertThat(streamResult).isEqualTo(functionResult);
    }
}
