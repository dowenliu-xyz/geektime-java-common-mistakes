package org.geektime.java.common.mistakes.extra.java8;


import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Java8 Lambda 示例
 * <p>create at 2020/8/8</p>
 *
 * @author liufl
 * @since 1.0
 */
public class LambdaTest {
    /**
     * 比较 Lambda 和匿名类
     *
     * @throws InterruptedException ignore
     */
    @SuppressWarnings("Convert2Lambda")
    @Test
    public void lambdaVsAnonymousClass() throws InterruptedException {
        // 传统匿名类写法
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("hello1");
            }
        }).start();
        // 等效的 Lambda 写法
        new Thread(() -> System.out.println("hello2")).start();

        Thread.sleep(1);
    }

    /**
     * 函数式接口示例
     */
    @Test
    public void functionalInterfaces() {
        // java.util.function包提供的函数接口类
        // Supplier
        Supplier<String> supplier = String::new;
        assertThat(supplier.get()).isEmpty();
        Supplier<String> stringSupplier = () -> "OK";
        assertThat(stringSupplier.get()).isEqualTo("OK");
        Supplier<Integer> random = () -> ThreadLocalRandom.current().nextInt();
        System.out.println(random.get());
        // Predicate
        Predicate<Integer> positiveNumber = i -> i > 0;
        Predicate<Integer> evenNumber = i -> i % 2 == 0;
        assertThat(positiveNumber.and(evenNumber).test(2)).isTrue();
        // Consumer
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream sysOut = System.out;
        System.setOut(new PrintStream(out));
        try {
            Consumer<String> println = System.out::println;
            println.andThen(println).accept("abcdefg");
        } finally {
            System.setOut(sysOut);
        }
        assertThat(new String(out.toByteArray(), StandardCharsets.UTF_8)).isEqualTo("abcdefg\nabcdefg\n");
        // Function
        Function<String, String> upperCase = String::toUpperCase;
        Function<String, String> duplicate = s -> s.concat(s);
        assertThat(upperCase.andThen(duplicate).apply("test")).isEqualTo("TESTTEST");
        // BinaryOperator
        BinaryOperator<Integer> add = Integer::sum;
        BinaryOperator<Integer> subtraction = (a, b) -> a - b;
        assertThat(subtraction.apply(add.apply(1, 2), 3)).isEqualTo(0);
    }
}
