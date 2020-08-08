package org.geektime.java.common.mistakes.extra.java8;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 利用 Stream 实现仿 grep 功能示例
 * <p>create at 2020/8/8</p>
 *
 * @author liufl
 * @since 1.0
 */
public class GrepLikeTest {
    @Test
    public void grep() throws IOException {
        // 无限深度，递归遍历文件夹
        try (Stream<Path> pathStream = Files.walk(Paths.get("."))) {
            pathStream.filter(Files::isRegularFile) // 只查看普通文件
                    .filter(FileSystems.getDefault().getPathMatcher("glob:**/*.java")::matches) // 搜索 java 源码文件
                    .flatMap(ThrowingFunction.unchecked(path ->
                            Files.lines(path) // 读取文件内容，转换为 Stream<List>
                                    .filter(line -> Pattern.compile("public class").matcher(line).find()) // 使用正则过滤带有 public class 的行
                                    .map(line -> path.getFileName() + " >> " + line))) // 把这行文件内容转换为文件名 + 行
                    .forEach(System.out::println);
        }
    }

    @FunctionalInterface
    public interface ThrowingFunction<T, R, E extends Throwable> {
        R apply(T t) throws E;

        static <T, R, E extends Throwable> Function<T, R> unchecked(ThrowingFunction<T, R, E> f) {
            return t -> {
                try {
                    return f.apply(t);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }
}
