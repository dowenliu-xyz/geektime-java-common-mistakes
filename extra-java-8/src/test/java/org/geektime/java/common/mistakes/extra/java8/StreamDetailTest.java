package org.geektime.java.common.mistakes.extra.java8;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p>create at 2020/8/8</p>
 *
 * @author liufl
 * @since 1.0
 */
public class StreamDetailTest {
    private static final Random random = new Random();
    private List<Order> orders;

    @BeforeEach
    public void data() {
        orders = Order.getData();

        orders.forEach(System.out::println);
        System.out.println("========================================");
    }

    @Test
    public void filter() {
        System.out.println("// 最近半年的金额大于40的订单");
        orders.stream()
                .filter(Objects::nonNull)
                .filter(order -> order.getPlacedAt().isAfter(LocalDateTime.now().minusMonths(6)))
                .filter(order -> order.getTotalPrice() > 40)
                .forEach(System.out::println);
    }

    @Test
    public void map() {
        // 计算所有订单商品数量
        // 通过两次遍历实现
        LongAdder longAdder = new LongAdder();
        //noinspection SimplifyStreamApiCallChains
        orders.stream().forEach(order ->
                order.getOrderItemList().forEach(orderItem -> longAdder.add(orderItem.getProductQuantity())));
        // 使用两次mapToLong + sum 实现
        long total = orders.stream().mapToLong(order ->
                order.getOrderItemList().stream().mapToLong(OrderItem::getProductQuantity).sum()
        ).sum();
        assertThat(total).isEqualTo(longAdder.longValue());
        // 把IntStream通过转换Stream<Product>
        System.out.println(IntStream.rangeClosed(1, 10).mapToObj(i ->
                new Product((long) i, "product" + i, i * 100.0))
                .collect(toList()));
    }

    @Test
    public void sorted() {
        System.out.println("// 大于50的订单，按照订单价格倒序前5");
        orders.stream().filter(order -> order.getTotalPrice() > 50)
                .sorted(comparing(Order::getTotalPrice).reversed())
                .limit(5)
                .forEach(System.out::println);
    }

    @Test
    public void flatMap() {
        // 依赖订单上的总价格字段
        System.out.println(orders.stream().mapToDouble(Order::getTotalPrice).sum());
        // 如果不依赖订单上的总价格，可以直接展开订单商品进行价格统计
        System.out.println(orders.stream().flatMap(order ->
                order.getOrderItemList().stream())
                .mapToDouble(item -> item.getProductPrice() * item.getProductQuantity()).sum());

        // 另一种方式 flatMap+mapToDouble=flatMapToDouble
        System.out.println(orders.stream().flatMapToDouble(order ->
                order.getOrderItemList().stream().mapToDouble(item ->
                        item.getProductPrice() * item.getProductQuantity()))
                .sum());
    }

    @Test
    public void groupBy() {
        System.out.println("// 按照用户名分组，统计下单数量");
        System.out.println(orders.stream().collect(groupingBy(Order::getCustomerName, Collectors.counting()))
                .entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed()).collect(toList()));

        System.out.println("// 按照用户名分组，统计订单总金额");
        System.out.println(orders.stream().collect(groupingBy(Order::getCustomerName, summingDouble(Order::getTotalPrice)))
                .entrySet().stream().sorted(Map.Entry.<String, Double>comparingByValue().reversed()).collect(toList()));

        System.out.println("// 按照用户名分组，统计商品采购数量");
        System.out.println(orders.stream().collect(groupingBy(Order::getCustomerName,
                summingInt(order -> order.getOrderItemList().stream().mapToInt(OrderItem::getProductQuantity).sum())))
                .entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).collect(toList()));

        System.out.println("// 统计最受欢迎的商品，倒序后取第一个");
        orders.stream().flatMap(order -> order.getOrderItemList().stream())
                .collect(groupingBy(OrderItem::getProductName, summingInt(OrderItem::getProductQuantity)))
                .entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .findFirst().ifPresent(System.out::println);

        System.out.println("// 统计最受欢迎的商品的另一种方式，直接利用maxBy");
        //noinspection SimplifyStreamApiCallChains
        orders.stream().flatMap(order -> order.getOrderItemList().stream())
                .collect(groupingBy(OrderItem::getProductName, summingInt(OrderItem::getProductQuantity)))
                .entrySet().stream().collect(maxBy(Map.Entry.comparingByValue())).map(Map.Entry::getKey)
                .ifPresent(System.out::println);
        System.out.println("// 或者再直接一点，max");
        orders.stream().flatMap(order -> order.getOrderItemList().stream())
                .collect(groupingBy(OrderItem::getProductName, summingInt(OrderItem::getProductQuantity)))
                .entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).ifPresent(System.out::println);

        System.out.println("// 按照用户名分组，选用户下的总金额最大的订单");
        orders.stream().collect(toMap(Order::getCustomerName, Function.identity(), BinaryOperator.maxBy(comparingDouble(Order::getTotalPrice))))
                .forEach((s, order) -> System.out.println(s + "#" + order.getTotalPrice() + "@" + order.getPlacedAt()));

        System.out.println("// 根据下年月分组统计订单ID列表");
        System.out.println(orders.stream().collect(groupingBy(order -> order.getPlacedAt().format(DateTimeFormatter.ofPattern("uuuuMM")), mapping(Order::getId, toList()))));

        System.out.println("// 根据下单年月+用户名两次分组，统计订单ID列表");
        System.out.println(orders.stream().collect(groupingBy(order -> order.getPlacedAt().format(DateTimeFormatter.ofPattern("uuuuMM")), groupingBy(Order::getCustomerName, mapping(Order::getId, toList())))));
    }

    @Test
    public void maxMin() {
        orders.stream().max(comparing(Order::getTotalPrice)).ifPresent(System.out::println);
        orders.stream().min(comparing(Order::getTotalPrice)).ifPresent(System.out::println);
    }

    @Test
    public void reduce() {
        System.out.println("// 统计花钱最多的人");
        System.out.println(orders.stream().collect(groupingBy(Order::getCustomerName, summingDouble(Order::getTotalPrice)))
                .entrySet().stream().reduce(BinaryOperator.maxBy(Map.Entry.comparingByValue()))
                .map(Map.Entry::getKey).orElse("N/A"));
    }

    @Test
    public void distinct() {
        System.out.println("// 不去重的下单用户");
        System.out.println(orders.stream().map(Order::getCustomerName).collect(joining(",")));

        System.out.println("// 去重的下单用户");
        System.out.println(orders.stream().map(Order::getCustomerName).distinct().collect(joining(",")));

        System.out.println("// 所有购买过的商品");
        System.out.println(orders.stream()
                .flatMap(order -> order.getOrderItemList().stream())
                .map(OrderItem::getProductName)
                .distinct().collect(joining(",")));
    }

    @Test
    public void collect() {
        System.out.println("// 生成一定位数的随机字符串");
        System.out.println(random.ints(48, 122)
                .filter(i -> (i < 57 || i > 65) && (i < 90 || i > 97))
                .mapToObj(i -> (char) i)
                .limit(20)
                .collect(StringBuffer::new, StringBuffer::append, StringBuffer::append)
                .toString());

        System.out.println("// 所有下单的用户，使用toSet去重了");
        System.out.println(orders.stream()
                .map(Order::getCustomerName).collect(toSet())
                .stream().collect(joining(",", "[", "]")));

        System.out.println("// 用toCollection收集器指定集合类型");
        System.out.println(orders.stream().limit(2).collect(toCollection(LinkedList::new)).getClass());

        System.out.println("// 使用toMap获取订单ID+下单用户名的Map");
        orders.stream()
                .collect(toMap(Order::getId, Order::getCustomerName))
                .entrySet().forEach(System.out::println);

        System.out.println("// 使用toMap获取下单用户名+最近一次下单时间的Map");
        orders.stream()
                .collect(toMap(Order::getCustomerName, Order::getPlacedAt, (x, y) -> x.isAfter(y) ? x : y))
                .entrySet().forEach(System.out::println);

        System.out.println("// 订单平均购买的商品数量");
        System.out.println(orders.stream().collect(averagingInt(order ->
                order.getOrderItemList().stream().mapToInt(OrderItem::getProductQuantity).sum())));
    }

    @Test
    public void partition() {
        // 先来看一下所有下单的用户
        orders.stream().map(Order::getCustomerName).collect(toSet()).forEach(System.out::println);
        // 根据是否有下单记录进行分区
        System.out.println(Customer.getData().stream().collect(partitioningBy(customer ->
                orders.stream().mapToLong(Order::getCustomerId).anyMatch(id -> id == customer.getId()))));
    }

    @Test
    public void skipLimit() {
        orders.stream()
                .sorted(comparing(Order::getPlacedAt))
                .map(order -> order.getCustomerName() + "@" + order.getPlacedAt())
                .limit(2).forEach(System.out::println);

        orders.stream()
                .sorted(comparing(Order::getPlacedAt))
                .map(order -> order.getCustomerName() + "@" + order.getPlacedAt())
                .skip(2).limit(2).forEach(System.out::println);
    }

    @Test
    public void peek() {
        IntStream.rangeClosed(1, 10)
                .peek(i -> {
                    System.out.println("第一次peek");
                    System.out.println(i);
                })
                .filter(i -> i > 5)
                .peek(i -> {
                    System.out.println("第二次peek");
                    System.out.println(i);
                })
                .filter(i -> i % 2 == 0)
                .forEach(i -> {
                    System.out.println("最终结果");
                    System.out.println(i);
                });

//        orders.stream()
//                .filter(order -> order.getTotalPrice() > 40)
//                .peek(order -> System.out.println(order.toString()))
//                .map(Order::getCustomerName)
//                .collect(toList());
    }
}
