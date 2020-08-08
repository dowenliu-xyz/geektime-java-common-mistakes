package org.geektime.java.common.mistakes.coding.lock;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = InterestingTest.class)
@Slf4j
public class InterestingTest {
    @Test
    @Disabled
    public void wrong() throws InterruptedException {
        final Interesting interesting = new Interesting();
        final CountDownLatch cdl = new CountDownLatch(2);
        new Thread(() -> {
            try {
                interesting.add();
            } finally {
                cdl.countDown();
            }
        }).start();
        new Thread(() -> {
            try {
                interesting.compare();
            } finally {
                cdl.countDown();
            }
        }).start();
        cdl.await();
        assertThat(interesting.getEqual()).isEqualTo(0);
    }

    @Test
    public void right() throws InterruptedException {
        final Interesting interesting = new Interesting();
        final CountDownLatch cdl = new CountDownLatch(2);
        new Thread(() -> {
            try {
                interesting.add();
            } finally {
                cdl.countDown();
            }
        }).start();
        new Thread(() -> {
            try {
                interesting.compareRight();
            } finally {
                cdl.countDown();
            }
        }).start();
        cdl.await();
        assertThat(interesting.getEqual()).isEqualTo(0);
    }
}