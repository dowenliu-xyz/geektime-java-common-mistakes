package org.geektime.java.common.mistakes.coding.concurrent.tools.thread.local;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>create at 2020/3/11</p>
 *
 * @author liufl
 * @since 1.0
 */
@SpringBootApplication
public class ThreadLocalMisuseApplication {
    public static void main(String[] args) {
        SpringApplication.run(ThreadLocalMisuseApplication.class, args);
    }
}
