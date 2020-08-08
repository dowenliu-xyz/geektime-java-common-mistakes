package org.geektime.java.common.mistakes.extra.java8;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.OptionalDouble;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link Optional} 示例
 * <p>create at 2020/8/8</p>
 *
 * @author liufl
 * @since 1.0
 */
public class OptionalTest {
    @Test
    public void optional() {
        // 通过 get 方法获取 Optional 中的实际值
        assertThat(Optional.of(1).get()).isEqualTo(1);
        // 通过 orNullable 来初始化一个 null ，
        // 通过 orElse 方法实现 Optional 中无数据的时候返回一个默认值
        //noinspection ConstantConditions
        assertThat(Optional.ofNullable(null).orElse("A")).isEqualTo("A");
        // OptionalDouble 是基本类型 double 的 Optional 对象， isPresent 判断有无数据
        //noinspection ConstantConditions
        assertThat(OptionalDouble.empty().isPresent()).isFalse();
        // 通过 map 方法可以对 Optional 对象进行级联转换，
        // 不会出现空指针，转换后还是一个 Optional
        assertThat(Optional.of(1).map(Math::incrementExact).get()).isEqualTo(2);
        // 通过 filter 实现 Optional 中数据的过滤，得到一个 Optional ，
        // 然后级联使用 orElse 提供默认值
        //noinspection ConstantConditions
        assertThat(Optional.of(1).filter(i -> i % 2 == 0).orElse(null)).isNull();
        // 通过 orElseThrow 实现无数据时抛出异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Optional.empty().orElseThrow(IllegalArgumentException::new));
        assertThat(exception).isNotNull();
    }
}
