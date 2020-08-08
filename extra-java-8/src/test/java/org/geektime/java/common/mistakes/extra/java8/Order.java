package org.geektime.java.common.mistakes.extra.java8;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * <p>create at 2020/8/8</p>
 *
 * @author liufl
 * @since 1.0
 */
@Data
public class Order {
    private final Long id;
    private final Long customerId;
    private final String customerName;
    private final List<OrderItem> orderItemList;
    private final Double totalPrice;
    private final LocalDateTime placedAt;

    public static List<Order> getData() {
        List<Product> products = Product.getData();
        List<Customer> customers = Customer.getData();

        Random random = new Random();
        return LongStream.rangeClosed(1, 10).mapToObj(i -> {
            Customer customer = customers.get(random.nextInt(customers.size()));
            List<OrderItem> orderItemList =
                    IntStream.rangeClosed(1, random.ints(1, 1, 8).findFirst().orElse(1)).mapToObj(j -> {
                        Product product = products.get(random.nextInt(products.size()));
                        return new OrderItem(
                                product.getId(),
                                product.getName(),
                                product.getPrice(),
                                random.ints(1, 1, 5).findFirst().orElse(1)
                        );
                    }).collect(Collectors.toList());
            return new Order(
                    i,
                    customer.getId(),
                    customer.getName(),
                    orderItemList,
                    orderItemList.stream().mapToDouble(item -> item.getProductPrice() * item.getProductQuantity()).sum(),
                    LocalDateTime.now().minusHours(random.nextInt(24 * 365))
            );
        }).collect(Collectors.toList());
    }
}
