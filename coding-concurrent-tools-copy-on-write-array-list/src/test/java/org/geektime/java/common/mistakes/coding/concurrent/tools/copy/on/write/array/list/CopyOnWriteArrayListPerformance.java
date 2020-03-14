package org.geektime.java.common.mistakes.coding.concurrent.tools.copy.on.write.array.list;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p>create at 2020/3/14</p>
 *
 * @author liufl
 * @since 1.0
 */
@SpringBootTest(classes = CopyOnWriteArrayListPerformance.class)
@RunWith(SpringRunner.class)
@Slf4j
public class CopyOnWriteArrayListPerformance {
    @Test
    public void benchmarkWrite() {
        List<Integer> copyOnWriteArrayList = new CopyOnWriteArrayList<>();
        List<Integer> synchronizedArrayList = Collections.synchronizedList(new ArrayList<>());
        final StopWatch stopWatch = new StopWatch();
        int loopCount = 100_000;
        stopWatch.start("Write:copyOnWriteArrayList");
        // 并发向 CopyOnWriteArrayList 写入 loopCount 次随机元素
        IntStream.rangeClosed(1, loopCount).parallel().forEach(value -> copyOnWriteArrayList.add(ThreadLocalRandom.current().nextInt(loopCount)));
        stopWatch.stop();
        final long copyOnWriteArrayListCoast = stopWatch.getLastTaskTimeNanos();
        stopWatch.start("Write:synchronizedArrayList");
        // 并发向 synchronized ArrayLis t写入 loopCount 次随机元素
        IntStream.rangeClosed(1, loopCount).parallel().forEach(value -> synchronizedArrayList.add(ThreadLocalRandom.current().nextInt(loopCount)));
        stopWatch.stop();
        final long synchronizedArrayListCoast = stopWatch.getLastTaskTimeNanos();
        log.info(stopWatch.prettyPrint());
        assertThat(copyOnWriteArrayList.size()).isEqualTo(loopCount);
        assertThat(synchronizedArrayList.size()).isEqualTo(loopCount);
        assertThat(copyOnWriteArrayListCoast >= synchronizedArrayListCoast)
                .withFailMessage("并发写入，CopyOnWriteArrayList 应该比 synchronized ArrayList 写入性能低")
                .isTrue();
        log.info("大量并发写入时，CopyOnWriteArrayList 性能远远低于通用并发方案 synchronized + ArrayList");
    }

    @Test
    public void benchmarkRead() {
        final CopyOnWriteArrayList<Integer> copyOnWriteArrayList = new CopyOnWriteArrayList<>();
        final List<Integer> synchronizedArrayList = Collections.synchronizedList(new ArrayList<>());
        final int count = 1_000_000;
        // 填充数据
        addAll(copyOnWriteArrayList, count);
        addAll(synchronizedArrayList, count);
        final StopWatch stopWatch = new StopWatch();
        final int loopCount = 1_000_000;
        stopWatch.start("Read:copyOnWriteArrayList");
        IntStream.rangeClosed(1, loopCount).parallel().forEach(value -> copyOnWriteArrayList.get(ThreadLocalRandom.current().nextInt(count)));
        stopWatch.stop();
        final long copyOnWriteArrayListCoast = stopWatch.getLastTaskTimeNanos();
        stopWatch.start("Read:synchronizedArrayList");
        IntStream.rangeClosed(1, loopCount).parallel().forEach(value -> synchronizedArrayList.get(ThreadLocalRandom.current().nextInt(count)));
        stopWatch.stop();
        final long synchronizedArrayListCoast = stopWatch.getLastTaskTimeNanos();
        log.info(stopWatch.prettyPrint());
        assertThat(copyOnWriteArrayListCoast < synchronizedArrayListCoast)
                .withFailMessage("并发读取，CopyOnWriteArrayList 应该比 synchronized ArrayList 读性能好")
                .isTrue();
    }

    // 填充 List
    private static void addAll(List<Integer> list, int size) {
        list.addAll(IntStream.rangeClosed(1, size).boxed().collect(Collectors.toList()));
    }
}
