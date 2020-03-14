package org.geektime.java.common.mistakes.coding.concurrent.tools.concurrent.hash.map;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p>create at 2020/3/14</p>
 *
 * @author liufl
 * @since 1.0
 */
@SpringBootTest(classes = ConcurrentHashMapPerformance.class)
@RunWith(SpringRunner.class)
@Slf4j
public class ConcurrentHashMapPerformance {
    // 循环次数
    private static final int LOOP_COUNT = 10_000_000;
    // 并发线程数量
    private static final int THREAD_COUNT = 10;
    // 元素数量
    private static final int ITEM_COUNT = 10;

    private static final ForkJoinPool FORK_JOIN_POOL = new ForkJoinPool(THREAD_COUNT);

    @Test
    public void normal() throws InterruptedException {
        ConcurrentHashMap<String, Long> freqs = new ConcurrentHashMap<>(ITEM_COUNT);
        final CountDownLatch cdl = new CountDownLatch(LOOP_COUNT);
        FORK_JOIN_POOL.execute(() -> IntStream.rangeClosed(1, LOOP_COUNT)
                .parallel()/*这里 parallel 方法很重要，如果去掉将在单线程中执行 */
                .forEach(i -> {
                    try {
                        // 获得一个随机的 Key
                        String key = "item" + ThreadLocalRandom.current().nextInt(ITEM_COUNT);
                        synchronized (freqs) {
                            if (freqs.containsKey(key)) {
                                // key 存在则 +1
                                freqs.put(key, freqs.get(key) + 1);
                            } else {
                                // key 不存在则初始化为 1
                                freqs.put(key, 1L);
                            }
                        }
                    } finally {
                        cdl.countDown();
                    }
                }));
        cdl.await();
        assertThat(freqs.size())
                .withFailMessage("size should be {}", ITEM_COUNT).isEqualTo(ITEM_COUNT);
        assertThat(freqs.values().stream().reduce(0L, Long::sum))
                .withFailMessage("total count should be {}", LOOP_COUNT).isEqualTo(LOOP_COUNT);
    }

    @Test
    public void good() throws InterruptedException {
        ConcurrentHashMap<String, LongAdder> addrs = new ConcurrentHashMap<>(ITEM_COUNT);
        final CountDownLatch cdl = new CountDownLatch(LOOP_COUNT);
        FORK_JOIN_POOL.execute(() -> IntStream.rangeClosed(1, LOOP_COUNT)
                .parallel()
                .forEach(i -> {
                    try {
                        String key = "item" + ThreadLocalRandom.current().nextInt(ITEM_COUNT);
                        addrs.computeIfAbsent(key, k -> new LongAdder()).increment();
                    } finally {
                        cdl.countDown();
                    }
                }));
        cdl.await();
        final Map<String, Long> freqs = addrs.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().longValue()));
        assertThat(freqs.size())
                .withFailMessage("size should be {}", ITEM_COUNT).isEqualTo(ITEM_COUNT);
        assertThat(freqs.values().stream().reduce(0L, Long::sum).longValue())
                .withFailMessage("total count should be {}", LOOP_COUNT).isEqualTo(LOOP_COUNT);
    }

    @Test
    public void benchmark() throws InterruptedException {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start("normal");
        normal();
        stopWatch.stop();
        final StopWatch.TaskInfo mormalTaskInfo = stopWatch.getLastTaskInfo();
        stopWatch.start("good");
        good();
        stopWatch.stop();
        final StopWatch.TaskInfo goodTaskInfo = stopWatch.getLastTaskInfo();
        log.info(stopWatch.prettyPrint());
        assertThat(goodTaskInfo.getTimeNanos()).isLessThanOrEqualTo(mormalTaskInfo.getTimeNanos());
    }
}
