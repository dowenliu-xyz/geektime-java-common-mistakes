package org.geektime.java.common.mistakes.coding.lock;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p>create at 2020/3/14</p>
 *
 * @author liufl
 * @since 1.0
 */
@SpringBootTest(classes = LockGranularity.class)
@Slf4j
public class LockGranularity {
    private List<Integer> data;

    private void slowButSafe() {
        try {
            TimeUnit.MILLISECONDS.sleep(10);
        } catch (InterruptedException ignored) {
        }
    }

    @BeforeEach
    public void prepareData() {
        this.data = new ArrayList<>();
    }

    @Test
    @Disabled
    public void wrong() {
        long begin = System.currentTimeMillis();
        IntStream.rangeClosed(1, 1000).parallel().forEach(value -> {
            synchronized (this) {
                slowButSafe();
                data.add(value);
            }
        });
        log.info("took: {}", System.currentTimeMillis() - begin);
        assertThat(data).hasSize(1000);
    }

    @Test
    public void right() {
        long begin = System.currentTimeMillis();
        IntStream.rangeClosed(1, 1000).parallel().forEach(value -> {
            slowButSafe();
            synchronized (this) {
                data.add(value);
            }
        });
        log.info("took: {}", System.currentTimeMillis() - begin);
        assertThat(data).hasSize(1000);
    }
}
