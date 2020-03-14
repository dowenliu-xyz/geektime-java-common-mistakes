package org.geektime.java.common.mistakes.coding.lock;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p>create at 2020/3/14</p>
 *
 * @author liufl
 * @since 1.0
 */
@SpringBootTest(classes = DataTest.class)
@RunWith(SpringRunner.class)
@Slf4j
public class DataTest {
    private static final int count = 1_000_000;

    @Test
    public void wrong() {
        Data.reset();
        IntStream.rangeClosed(1, count).parallel().forEach(value -> new Data().addWrong());
        log.info("result counter: {}", Data.getCounter());
        assertThat(Data.getCounter()).isEqualTo(count);
    }

    @Test
    public void right() {
        Data.reset();
        IntStream.rangeClosed(1, count).parallel().forEach(value -> new Data().addRight());
        log.info("result counter: {}", Data.getCounter());
        assertThat(Data.getCounter()).isEqualTo(count);
    }
}
