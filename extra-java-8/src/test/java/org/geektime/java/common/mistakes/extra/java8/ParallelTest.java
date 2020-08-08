package org.geektime.java.common.mistakes.extra.java8;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 并行 {@link Stream} 示例
 * <p>create at 2020/8/8</p>
 *
 * @author liufl
 * @since 1.0
 */
@Slf4j
public class ParallelTest {
    /**
     * 通过线程池并行消费处理1到100
     */
    @Test
    public void parallelConsume() {
        IntStream.rangeClosed(1, 100).parallel().forEach(i -> {
            log.info("{}", i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        });
    }

    /**
     * 并行批量处理的各种方式。<br/>
     * 假设场景：使用 20 个线程（threadCount）以并行方式总计执行 10000 次（taskCount）操作。
     * 单个任务单线程执行需要耗时10毫秒。
     */
    @Test
    public void allMethods() throws InterruptedException, ExecutionException {
        int taskCount = 10000;
        int threadCount = 20;
        StopWatch stopWatch = new StopWatch();

        stopWatch.start("thread");
        assertThat(thread(taskCount, threadCount)).isEqualTo(taskCount);
        stopWatch.stop();

        stopWatch.start("threadPool");
        assertThat(threadPool(taskCount, threadCount)).isEqualTo(taskCount);
        stopWatch.stop();

        stopWatch.start("stream");
        assertThat(stream(taskCount, threadCount)).isEqualTo(taskCount);
        stopWatch.stop();

        stopWatch.start("forkJoinPool");
        assertThat(forkJoinPool(taskCount, threadCount)).isEqualTo(taskCount);
        stopWatch.stop();

        stopWatch.start("completableFuture");
        assertThat(completableFuture(taskCount, threadCount)).isEqualTo(taskCount);
        stopWatch.stop();

        log.info(stopWatch.prettyPrint());
    }

    /**
     * 基础操作，耗时10毫秒
     */
    private void increment(AtomicInteger atomicInteger) {
        atomicInteger.incrementAndGet();
        try {
            TimeUnit.MILLISECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 直接手动分配线程方式
     */
    private int thread(int taskCount, int threadCount) throws InterruptedException {
        // 总操作次数计数器
        AtomicInteger atomicInteger = new AtomicInteger();
        // 使用 CountDownLatch 来等待所有线程执行完成
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        // 使用 IntStream 把数字直接转为 Thread
        IntStream.rangeClosed(1, threadCount).mapToObj(i -> new Thread(() -> {
            // 手动把 taskCount 分成 threadCount 份，每一份有一个线程执行
            IntStream.rangeClosed(1, taskCount / threadCount).forEach(j -> increment(atomicInteger));
            // 每一个线程处理完成自己那部分数据之后，countDown 一次
            countDownLatch.countDown();
        })).forEach(Thread::start);
        // 等到所有线程执行完成
        countDownLatch.await();
        // 查询计数器当前值
        return atomicInteger.get();
    }

    /**
     * 使用 Executors.newFixedThreadPool 来执行。<br/>
     * 注意，实际生产手工 new {@link java.util.concurrent.ThreadPoolExecutor} 来使用，不要使用 Executors 工具类
     */
    private int threadPool(int taskCount, int threadCount) throws InterruptedException {
        // 总操作次数计数器
        AtomicInteger atomicInteger = new AtomicInteger();
        // 初始化一个线程数量=threadCount的线程池
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        // 所有任务直接提交到线程池处理
        IntStream.rangeClosed(1, taskCount).forEach(i -> executorService.execute(() -> increment(atomicInteger)));
        // 提交关闭线程池申请，等待之前所有任务执行完成。
        // 实际生产中请使用 CountDownLatch 等同步器，不要随意创建销毁线程池，而是要尽可能重用（但不要跨任务重用）
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);
        // 查询计数器当前值
        return atomicInteger.get();
    }

    /**
     * 使用 ForkJoinPool 线程池执行
     */
    private int forkJoinPool(int taskCount, int threadCount) throws InterruptedException {
        // 总操作次数计数器
        AtomicInteger atomicInteger = new AtomicInteger();
        // 自定义一个并行度=threadCount的ForkJoinPool
        ForkJoinPool forkJoinPool = new ForkJoinPool(threadCount);
        // 所有任务直接提交到线程处理
        forkJoinPool.execute(() -> IntStream.rangeClosed(1, taskCount).parallel().forEach(i -> increment(atomicInteger)));
        // 提交关闭线程池申请，等待之前所有任务执行完成
        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(1, TimeUnit.HOURS);
        // 查询计数器当前值
        return atomicInteger.get();
    }

    /**
     * 使用公用 ForkJoinPool 执行
     */
    private int stream(int taskCount, int threadCount) {
        // 设置公共 ForkJoinPool的并行度
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(threadCount));
        // 总操作次数计数器
        AtomicInteger atomicInteger = new AtomicInteger();
        // 由于我们设置了公共 ForkJoinPool 的并行度，直接使用 parallel 提交任务即可
        IntStream.rangeClosed(1, taskCount).parallel().forEach(i -> increment(atomicInteger));
        // 查询计数器当前值
        return atomicInteger.get();
    }

    /**
     * 使用 CompletableFuture 实现
     */
    private int completableFuture(int taskCount, int threadCount) throws InterruptedException, ExecutionException {
        // 总操作次数计数器
        AtomicInteger atomicInteger = new AtomicInteger();
        // 自定义一个并行度=threadCount的ForkJoinPool
        ForkJoinPool forkJoinPool = new ForkJoinPool(threadCount);
        // 使用CompletableFuture.runAsync通过指定线程池异步执行任务
        CompletableFuture.runAsync(() -> IntStream.rangeClosed(1, taskCount).parallel().forEach(i -> increment(atomicInteger)), forkJoinPool).get();
        // 查询计数器当前值
        return atomicInteger.get();
    }
}
