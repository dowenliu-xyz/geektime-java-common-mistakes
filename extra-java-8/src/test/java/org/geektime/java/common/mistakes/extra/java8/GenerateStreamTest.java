package org.geektime.java.common.mistakes.extra.java8;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p>create at 2020/8/8</p>
 *
 * @author liufl
 * @since 1.0
 */
public class GenerateStreamTest {
    private ByteArrayOutputStream arrayOut;
    private PrintStream sysOut;

    @BeforeEach
    public void prepareOut() {
        arrayOut = new ByteArrayOutputStream();
        sysOut = System.out;
        System.setOut(new PrintStream(arrayOut));
    }

    @AfterEach
    public void restoreSysOut() {
        Optional.ofNullable(sysOut).ifPresent(System::setOut);
    }

    @Test
    public void of1() {
        String[] array = {"a", "b", "c"};
        Stream.of(array).forEach(System.out::println);
        assertThat(new String(arrayOut.toByteArray())).isEqualTo("a\nb\nc\n");
    }

    @Test
    public void of2() {
        Stream.of("a", "b", "c").forEach(System.out::println);
        assertThat(new String(arrayOut.toByteArray())).isEqualTo("a\nb\nc\n");
    }

    @Test
    public void of3() {
        Stream.of(1, 2, "a").map(item -> item.getClass().getName()).forEach(System.out::println);
        assertThat(new String(arrayOut.toByteArray())).isEqualTo("java.lang.Integer\njava.lang.Integer\njava.lang.String\n");
    }

    @Test
    public void iterate1() {
        Stream.iterate(2, item -> item * 2).limit(10).forEach(System.out::println);
        assertThat(new String(arrayOut.toByteArray())).isEqualTo("2\n4\n8\n16\n32\n64\n128\n256\n512\n1024\n");
    }

    @Test
    public void iterate2() {
        Stream.iterate(BigInteger.ZERO, n -> n.add(BigInteger.TEN)).limit(10).forEach(System.out::println);
        assertThat(new String(arrayOut.toByteArray())).isEqualTo("0\n10\n20\n30\n40\n50\n60\n70\n80\n90\n");
    }

    @Test
    public void generate1() {
        Stream.generate(() -> "test").limit(3).forEach(System.out::println);
        assertThat(new String(arrayOut.toByteArray())).isEqualTo("test\ntest\ntest\n");
    }

    @Test
    public void generate2() {
        Stream.generate(Math::random).limit(10).forEach(System.out::println);
        assertThat(new String(arrayOut.toByteArray())).matches("(?:0(?:\\.\\d+)\\n){10}");
    }

    @SuppressWarnings("SimplifyStreamApiCallChains")
    @Test
    public void stream1() {
        Arrays.asList("a1", "a2", "a3").stream().forEach(System.out::println);
        assertThat(new String(arrayOut.toByteArray())).isEqualTo("a1\na2\na3\n");
    }

    @Test
    public void stream2() {
        Arrays.stream(new int[]{1, 2, 3}).forEach(System.out::println);
        assertThat(new String(arrayOut.toByteArray())).isEqualTo("1\n2\n3\n");
    }

    @Test
    public void primitive1() {
        IntStream.range(1, 3).forEach(System.out::println);
        assertThat(new String(arrayOut.toByteArray())).isEqualTo("1\n2\n");
    }

    @Test
    public void primitive2() {
        IntStream.range(0, 3).mapToObj(i -> "x").forEach(System.out::println);
        assertThat(new String(arrayOut.toByteArray())).isEqualTo("x\nx\nx\n");
    }

    @Test
    public void primitive3() {
        IntStream.rangeClosed(1, 3).forEach(System.out::println);
        assertThat(new String(arrayOut.toByteArray())).isEqualTo("1\n2\n3\n");
    }

    @Test
    public void primitive4() {
        DoubleStream.of(1.1, 2.2, 3.3).forEach(System.out::println);
        assertThat(new String(arrayOut.toByteArray())).isEqualTo("1.1\n2.2\n3.3\n");
    }

    @Test
    public void primitive5() {
        assertThat(IntStream.of(1, 2).toArray().getClass().toString()).isEqualTo("class [I");
        assertThat(Stream.of(1, 2).mapToInt(Integer::intValue).toArray().getClass().toString()).isEqualTo("class [I");
        assertThat(IntStream.of(1, 2).boxed().toArray().getClass().toString()).isEqualTo("class [Ljava.lang.Object;");
        assertThat(IntStream.of(1, 2).asDoubleStream().toArray().getClass().toString()).isEqualTo("class [D");
        assertThat(IntStream.of(1, 2).asLongStream().toArray().getClass().toString()).isEqualTo("class [J");
    }

    @SuppressWarnings("SimplifyStreamApiCallChains")
    @Test
    public void primitive6() {
        List<String> list = Arrays.asList("a", "b", "c").stream() // Stream<String>
                .mapToInt(String::length) // IntStream
                .asLongStream() // LongStream
                .mapToDouble(x -> x / 10.0) // DoubleStream
                .boxed() // Stream<Double>
                .mapToLong(x -> 1L) // LongStream
                .mapToObj(x -> "") // Stream<String>
                .collect(Collectors.toList());
        assertThat(list).isNotNull().hasSize(3).allMatch(String::isEmpty);
    }
}
