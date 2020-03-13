# Java Common Mistakes
《[极客时间](https://time.geekbang.org/)》- 朱晔《[Java 业务开发常见错误 100 例](https://time.geekbang.org/column/intro/294)》 上手跟学代码。

官方代码库 [java-common-mistakes](https://github.com/JosephZhu1983/java-common-mistakes)

## 编码问题

### 并发工具问题

#### 在 Web 容器等多线程环境中 ThreadLocal 误用问题

* 不能认为没有显式开启多线程就不会有线程安全问题。
* 在 Tomcat 这种 Web 服务器下跑的业务代码，本来就运行在一个多线程环境。
* Tomcat 的工作线程是基于线程池的。
* 线程池会重用固定的几个线程，一旦线程重用，那么很可能首次从 ThreadLocal 获取的值是之前其他线程遗留的值。
* 使用类似 ThreadLocal 工具来存放一些数据时，需要特别注意在代码运行完后，显式地去清空设置的数据。

##### Mistake usage

Coding: [ThreadLocalMisuseController#wrong](./coding-concurrent-tools-web-thread-local/src/main/java/org/geektime/java/common/mistakes/coding/concurrent/tools/thread/local/ThreadLocalMisuseController.java#L27)
Testing: [ThreadLocalMisuseControllerTest#wrong](./coding-concurrent-tools-web-thread-local/src/test/java/org/geektime/java/common/mistakes/coding/concurrent/tools/thread/local/ThreadLocalMisuseControllerTest.java#L27)

##### Correct usage

Coding: [ThreadLocalMisuseController#right](./coding-concurrent-tools-web-thread-local/src/main/java/org/geektime/java/common/mistakes/coding/concurrent/tools/thread/local/ThreadLocalMisuseController.java#L38)
Testing: [ThreadLocalMisuseControllerTest#right](./coding-concurrent-tools-web-thread-local/src/test/java/org/geektime/java/common/mistakes/coding/concurrent/tools/thread/local/ThreadLocalMisuseControllerTest.java#L55)

#### ConcurrentHashMap 是线程安全的，并不代表一定线程安全

* 线程安全的并发容器只能保证方法级的操作原子性，多个操作间没有安全性保证
* ConcurrentHashMap 的能力有限：
    * 使用了 ConcurrentHashMap，不代表对它的多个操作之间的状态是一致的，是没有其他线程在操作它的，如果需要确保需要手动加锁。
    * 诸如 size、isEmpty 和 containsValue 等聚合方法，在并发情况下可能会反映 ConcurrentHashMap 的中间状态。因此在并发情况下，这些方法的返回值只能用作参考，而不能用于流程控制。显然，利用 size 方法计算差异值，是一个流程控制。
    * 诸如 putAll 这样的聚合方法也不能确保原子性，在 putAll 的过程中去获取数据可能会获取到部分数据。

##### Mistake usage

Testing: [ConcurrentHashMapMisuse#wrong](./coding-concurrent-tools-concurrent-hash-map/src/test/java/org/geektime/java/common/mistakes/coding/concurrent/tools/concurrent/hash/map/ConcurrentHashMapMisuse.java#L43)

##### Correct usage

Testing: [ConcurrentHashMapMisuse#right](./coding-concurrent-tools-concurrent-hash-map/src/test/java/org/geektime/java/common/mistakes/coding/concurrent/tools/concurrent/hash/map/ConcurrentHashMapMisuse.java#L63)
