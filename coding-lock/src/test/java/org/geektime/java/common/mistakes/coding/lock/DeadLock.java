package org.geektime.java.common.mistakes.coding.lock;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p>create at 2020/3/14</p>
 *
 * @author liufl
 * @since 1.0
 */
@SpringBootTest(classes = DeadLock.class)
@Slf4j
public class DeadLock {
    @Data
    @RequiredArgsConstructor
    static class Item {
        final String name;
        int stock = 1000;
        @ToString.Exclude
        ReentrantLock lock = new ReentrantLock();
    }

    private final ConcurrentHashMap<String, Item> items = new ConcurrentHashMap<>();

    @BeforeEach
    public void prepareData() {
        this.items.clear();
        IntStream.range(0, 10).forEach(i -> items.put("item" + i, new Item("item" + i)));
    }

    private boolean createOrder(List<Item> order) {
        List<ReentrantLock> locks = new ArrayList<>(order.size());

        for (Item item : order) {
            try {
                if (item.lock.tryLock(10, TimeUnit.SECONDS)) {
                    locks.add(item.lock);
                } else {
                    locks.forEach(ReentrantLock::unlock);
                    return false;
                }
            } catch (InterruptedException ignored) {
            }
        }
        try {
            order.forEach(item -> item.stock--);
        } finally {
            locks.forEach(ReentrantLock::unlock);
        }
        return true;
    }

    private List<Item> createCart() {
        return IntStream.rangeClosed(1, 3)
                .mapToObj(i -> "item" + ThreadLocalRandom.current().nextInt(items.size()))
                .map(items::get).collect(Collectors.toList());
    }

    @Test
    @Disabled
    public void wrong() {
        long begin = System.currentTimeMillis();
        final long success = IntStream.rangeClosed(1, 100).parallel().mapToObj(i -> {
            List<Item> cart = createCart();
            return createOrder(cart);
        }).filter(result -> result).count();
        final long totalRemaning = items.values().stream().map(item -> item.stock).reduce(0, Integer::sum).longValue();
        log.info("success: {} totalRemaining: {} took: {}ms items: {}",
                success, totalRemaning,
                System.currentTimeMillis() - begin, items);
        assertThat(10_000L - success * 3).isEqualTo(totalRemaning);
        assertThat(success).isEqualTo(100);
    }

    @Test
    public void right() {
        long begin = System.currentTimeMillis();
        final long success = IntStream.rangeClosed(1, 100).parallel().mapToObj(i -> {
            List<Item> cart = createCart()
                    .stream().sorted(Comparator.comparing(Item::getName))
                    .collect(Collectors.toList());
            return createOrder(cart);
        }).filter(result -> result).count();
        final long totalRemaning = items.values().stream().map(item -> item.stock).reduce(0, Integer::sum).longValue();
        log.info("success: {} totalRemaining: {} took: {}ms items: {}",
                success, totalRemaning,
                System.currentTimeMillis() - begin, items);
        assertThat(10_000L - success * 3).isEqualTo(totalRemaning);
        assertThat(success).isEqualTo(100);
    }
}
