package org.geektime.java.common.mistakes.extra.java8;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * <p>create at 2020/8/8</p>
 *
 * @author liufl
 * @since 1.0
 */
@Data
public class Customer {
    private final Long id;
    private final String name;

    public static List<Customer> getData() {
        return Arrays.asList(
                new Customer(10L, "小张"),
                new Customer(11L, "小王"),
                new Customer(12L, "小李"),
                new Customer(13L, "小朱"),
                new Customer(14L, "小徐")
        );
    }
}
