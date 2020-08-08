package org.geektime.java.common.mistakes.extra.java8;

import lombok.Data;

/**
 * <p>create at 2020/8/8</p>
 *
 * @author liufl
 * @since 1.0
 */
@Data
public class OrderItem {
    private final Long productId;
    private final String productName;
    private final Double productPrice;
    private final Integer productQuantity;
}
