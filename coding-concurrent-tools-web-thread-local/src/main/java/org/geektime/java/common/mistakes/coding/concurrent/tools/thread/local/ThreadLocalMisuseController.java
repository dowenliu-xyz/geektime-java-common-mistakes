package org.geektime.java.common.mistakes.coding.concurrent.tools.thread.local;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>create at 2020/3/11</p>
 *
 * @author liufl
 * @since 1.0
 */
@RestController
@RequestMapping("/thread-local")
public class ThreadLocalMisuseController {
    private ThreadLocal<Integer> currentUser = ThreadLocal.withInitial(() -> null);

    private String current() {
        return Thread.currentThread().getName() + ":" + currentUser.get();
    }

    @GetMapping("/wrong")
    public Map<String, String> wrong(@RequestParam("userId") Integer userId) {
        String before = current();
        currentUser.set(userId);
        String after = current();
        Map<String, String> result = new HashMap<>();
        result.put("before", before);
        result.put("after", after);
        return result;
    }

    @GetMapping("/right")
    public Map<String, String> right(@RequestParam("userId") Integer userId) {
        String before = current();
        currentUser.set(userId);
        try {
            String after = current();
            Map<String, String> result = new HashMap<>();
            result.put("before", before);
            result.put("after", after);
            return result;
        } finally {
            currentUser.remove();
        }
    }
}
