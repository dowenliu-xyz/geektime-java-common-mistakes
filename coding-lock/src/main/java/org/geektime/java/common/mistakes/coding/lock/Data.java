package org.geektime.java.common.mistakes.coding.lock;

import lombok.Getter;

/**
 * <p>create at 2020/3/14</p>
 *
 * @author liufl
 * @since 1.0
 */
public class Data {
    @Getter
    private static int counter = 0;
    private static Object locker = new Object();

    public static int reset() {
        counter = 0;
        return counter;
    }

    public synchronized void addWrong() {
        counter++;
    }

    public void addRight() {
        synchronized (locker) {
            counter++;
        }
    }
}
