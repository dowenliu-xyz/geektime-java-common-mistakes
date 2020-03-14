package org.geektime.java.common.mistakes.coding.lock;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = InterestingTest.class)
@RunWith(SpringRunner.class)
@Slf4j
public class InterestingTest {
    @Test
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