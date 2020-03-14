package org.geektime.java.common.mistakes.coding.lock;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>create at 2020/3/14</p>
 *
 * @author liufl
 * @since 1.0
 */
@Slf4j
public class Interesting {
    volatile int a = 1;
    volatile int b = 1;

    private final AtomicInteger equal = new AtomicInteger(0);

    public int getEqual() {
        return equal.get();
    }

    public synchronized void add() {
        log.info("add start");
        for (int i = 0; i < 1_000_000; i++) {
            a++;
            b++;
        }
        log.info("add done");
    }

    public void compare() {
        log.info("compare start");
        for (int i = 0; i < 1_000_000; i++) {
            if (a < b) {
                log.info("I see: a: {} < b: {}", a, b);
                equal.compareAndSet(0, 1);
                if (a > b) {
                    log.warn("But now, a: {} > b: {}!!!", a, b);
                    equal.compareAndSet(1, 2);
                }
            }
        }
        log.info("compare done");
    }

    public synchronized void compareRight() {
        compare();
    }
}
