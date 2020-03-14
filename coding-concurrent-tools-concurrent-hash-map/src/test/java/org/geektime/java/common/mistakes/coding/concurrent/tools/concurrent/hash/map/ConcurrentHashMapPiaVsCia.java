package org.geektime.java.common.mistakes.coding.concurrent.tools.concurrent.hash.map;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * <p>create at 2020/3/14</p>
 *
 * @author liufl
 * @since 1.0
 */
@SpringBootTest(classes = ConcurrentHashMapPiaVsCia.class)
@RunWith(SpringRunner.class)
@Slf4j
public class ConcurrentHashMapPiaVsCia {
    private static String getDelayedValue() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException ignored) {
        }
        return UUID.randomUUID().toString();
    }

    @Test
    public void test() {
        final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

        // null -> putIfAbsent
        try {
            log.info("putIfAbsent null value: {}", map.putIfAbsent("test1", null));
            fail("putIfAbsent null value on ConcurrentHashMap");
        } catch (Exception ex) {
            log.info("ConcurrentHashMap#putIfAbsent 不接受 null 参数，否则将抛出: {}", ex.toString());
        }
        assertThat(map).doesNotContainKey("test1");
        // null -> computeIfAbsent
        final String test2 = map.computeIfAbsent("test2", k -> null);
        assertThat(test2).isNull();
        assertThat(map).doesNotContainKey("test2");
        log.info("ConcurrentHashMap#computeIfAbsent Function 参数如果计算结果为 null ，返回结果为: {}， key 也不会被加入", test2);
        // non-null -> putIfAbsent
        final String test3 = map.putIfAbsent("test3", "test3");
        assertThat(test3).isNull();
        assertThat(map).containsKey("test3");
        assertThat(map.get("test3")).isNotNull();
        log.info("ConcurrentHashMap#putIfAbsent 接受 non-null 参数，key 不存在时，返回结果为 null， kv 成功被加入。使用结果时，注意NPE");
        // non-null -> computeIfAbsent
        final String test4 = map.computeIfAbsent("test4", k -> "test4");
        assertThat(test4).isNotNull();
        assertThat(map).containsKey("test4");
        assertThat(map.get("test4")).isEqualTo(test4);
        log.info("ConcurrentHashMap#computeIfAbsent Function 参数如果计算结果不为 null ，返回结果也不为 null， kv 成功被加入");

        // kv已存在时，耗时操作
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start("putIfAbsentWithDelayedValue");
        String newTest4 = map.putIfAbsent("test4", getDelayedValue());
        stopWatch.stop();
        assertThat(newTest4).isEqualTo(map.get("test4")).isEqualTo("test4");
        final long piaCoast = stopWatch.getLastTaskTimeMillis();
        assertThat(piaCoast).isGreaterThanOrEqualTo(1000);
        log.info("putIfAbsent 不论原 KV 是否存在，都要计算兜底值");
        stopWatch.start("computeIfAbsentWithDelayedValue");
        newTest4 = map.computeIfAbsent("test4", k -> getDelayedValue());
        stopWatch.stop();
        assertThat(newTest4).isEqualTo(map.get("test4")).isEqualTo("test4");
        final long ciaCoast = stopWatch.getLastTaskTimeMillis();
        assertThat(ciaCoast).isLessThan(1000);
        log.info("computeIfAbsent 只在原 KV 不存在时，才计算兜底值");
    }
}
