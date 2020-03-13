package org.geektime.java.common.mistakes.coding.concurrent.tools.concurrent.hash.map;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p>create at 2020/3/13</p>
 *
 * @author liufl
 * @since 1.0
 */
@SpringBootTest(classes = ConcurrentHashMapMisuse.class)
@RunWith(SpringRunner.class)
@Slf4j
public class ConcurrentHashMapMisuse {
    private static int THREAD_COUNT = 10;
    private static int ITEM_COUNT = 1000;

    private ConcurrentHashMap<String, Long> getData(int count) {
        return LongStream.rangeClosed(1, count)
                .boxed()
                .collect(Collectors.toConcurrentMap(
                        l -> UUID.randomUUID().toString(),
                        Function.identity(),
                        (l1, l2) -> l1, ConcurrentHashMap::new));
    }

    @Test
    public void wrong() throws InterruptedException {
        ConcurrentHashMap<String, Long> concurrentHashMap = getData(ITEM_COUNT - 100);
        log.info("init size: {}", concurrentHashMap.size());

        final ForkJoinPool forkJoinPool = new ForkJoinPool(THREAD_COUNT);
        forkJoinPool.execute(() -> IntStream.rangeClosed(1, 10).parallel().forEach(i -> {
            int gap = ITEM_COUNT - concurrentHashMap.size();
            log.info("gap size(): {}", gap);
            concurrentHashMap.putAll(getData(gap));
        }));
        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(1, TimeUnit.MINUTES);

        final int finishSize = concurrentHashMap.size();
        log.info("finish size:{}", finishSize);
        assertThat(finishSize).isGreaterThan(ITEM_COUNT);
        log.error("finish size [{}] is greeter than expected size [{}]", finishSize, ITEM_COUNT);
    }

    @Test
    public void right() throws InterruptedException {
        ConcurrentHashMap<String, Long> concurrentHashMap = getData(ITEM_COUNT - 100);
        log.info("init size: {}", concurrentHashMap.size());

        final ForkJoinPool forkJoinPool = new ForkJoinPool(THREAD_COUNT);
        forkJoinPool.execute(() -> IntStream.rangeClosed(1, 10).parallel().forEach(i -> {
            synchronized (concurrentHashMap) {
                int gap = ITEM_COUNT - concurrentHashMap.size();
                log.info("gap size(): {}", gap);
                concurrentHashMap.putAll(getData(gap));
                assertThat(gap).isIn(0, 100);
            }
        }));
        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(1, TimeUnit.MINUTES);

        final int finishSize = concurrentHashMap.size();
        log.info("finish size:{}", finishSize);
        assertThat(finishSize).isEqualTo(ITEM_COUNT);
    }
}
