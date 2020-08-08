package org.geektime.java.common.mistakes.extra.java8;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 使用 Stream 操作实现缓存，并与不用 Stream 操作对比
 * <p>create at 2020/8/8</p>
 *
 * @author liufl
 * @since 1.0
 */
public class CoolCacheTest {
    private Map<Long, Product> cache;

    @BeforeEach
    public void initCache() {
        cache = new ConcurrentHashMap<>();
    }

    /**
     * 传统方式获取产品数据并缓存
     */
    private Product getProductAndCache(Long id) {
        Product product = null;
        // Key 存在，返回 Value
        if (cache.containsKey(id)) {
            product = cache.get(id);
        } else {
            // 不存在，则获取 Value
            // 需要遍历数据源查询获取 Product
            for (Product p : Product.getData()) {
                if (p.getId().equals(id)) {
                    product = p;
                    break;
                }
            }
            // 加入 ConcurrentHashMap
            if (product != null) {
                cache.put(id, product);
            }
        }
        return product;
    }

    @Test
    public void notCoolCache() {
        assertThat(getProductAndCache(1L)).isNotNull();
        assertThat(getProductAndCache(100L)).isNull();
        System.out.println(cache);
        assertThat(cache.size()).isEqualTo(1);
        assertThat(cache.containsKey(1L)).isTrue();
        assertThat(cache.get(1L)).isNotNull();
        assertThat(cache.containsKey(100L)).isFalse();
    }

    private Product getProductAndCacheCool(Long id) {
        return cache.computeIfAbsent(id, i -> // 当 Key 不存在时，提供一个 Function 来代表根据 Key 获取 Value 的过程
                Product.getData().stream()
                        .filter(p -> p.getId().equals(i)) // 过滤
                        .findFirst() // 找到第一个，得到 Optional<Product>
                        .orElse(null)); // 如果找不到 Product，则使用 null。Function 返回 null，则 ConcurrentHashMap 不存储对应的 K/V
    }

    @Test
    public void coolCache() {
        assertThat(getProductAndCacheCool(1L)).isNotNull();
        assertThat(getProductAndCacheCool(100L)).isNull();
        System.out.println(cache);
        assertThat(cache.size()).isEqualTo(1);
        assertThat(cache.containsKey(1L)).isTrue();
        assertThat(cache.get(1L)).isNotNull();
        assertThat(cache.containsKey(100L)).isFalse();
    }
}
