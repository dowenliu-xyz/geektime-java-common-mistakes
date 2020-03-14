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

##### 场景

使用 ThreadLocal 缓存请求业务过程中要使用的信息

* 错误用法

Coding: [ThreadLocalMisuseController#wrong](./coding-concurrent-tools-web-thread-local/src/main/java/org/geektime/java/common/mistakes/coding/concurrent/tools/thread/local/ThreadLocalMisuseController.java#L27)
Testing: [ThreadLocalMisuseControllerTest#wrong](./coding-concurrent-tools-web-thread-local/src/test/java/org/geektime/java/common/mistakes/coding/concurrent/tools/thread/local/ThreadLocalMisuseControllerTest.java#L27)

* 正确用法

Coding: [ThreadLocalMisuseController#right](./coding-concurrent-tools-web-thread-local/src/main/java/org/geektime/java/common/mistakes/coding/concurrent/tools/thread/local/ThreadLocalMisuseController.java#L38)
Testing: [ThreadLocalMisuseControllerTest#right](./coding-concurrent-tools-web-thread-local/src/test/java/org/geektime/java/common/mistakes/coding/concurrent/tools/thread/local/ThreadLocalMisuseControllerTest.java#L55)

#### ConcurrentHashMap 是线程安全的，并不代表一定线程安全

* 线程安全的并发容器只能保证方法级的操作原子性，多个操作间没有安全性保证
* ConcurrentHashMap 的能力有限：
    * 使用了 ConcurrentHashMap，不代表对它的多个操作之间的状态是一致的，是没有其他线程在操作它的，如果需要确保需要手动加锁。
    * 诸如 size、isEmpty 和 containsValue 等聚合方法，在并发情况下可能会反映 ConcurrentHashMap 的中间状态。因此在并发情况下，这些方法的返回值只能用作参考，而不能用于流程控制。显然，利用 size 方法计算差异值，是一个流程控制。
    * 诸如 putAll 这样的聚合方法也不能确保原子性，在 putAll 的过程中去获取数据可能会获取到部分数据。

##### 场景

一个 ConcurrentHashMap 已有 900 条数据，需要将它填满到恰好 1000 条数据。10 个线程并发尝试进行填满操作。

* 错误用法

Testing: [ConcurrentHashMapMisuse#wrong](./coding-concurrent-tools-concurrent-hash-map/src/test/java/org/geektime/java/common/mistakes/coding/concurrent/tools/concurrent/hash/map/ConcurrentHashMapMisuse.java#L43)

* 正确用法

Testing: [ConcurrentHashMapMisuse#right](./coding-concurrent-tools-concurrent-hash-map/src/test/java/org/geektime/java/common/mistakes/coding/concurrent/tools/concurrent/hash/map/ConcurrentHashMapMisuse.java#L63)

#### 充分发挥 ConcurrentHashMap 性能

* ConcurrentHashMap 的 computeIfAbsent 方法是原子性方法
* 并发计数场景可以考虑 LongAdder
* 像 ConcurrentHashMap 这样的高级并发工具的确提供了一些高级 API，只有充分了解其特性才能最大化其威力，而不能因为其足够高级、酷炫盲目使用。

##### 场景

使用 Map 统计 Key 出现次数。Key的取值 0 ~ 9 。10 个线程并发计数。

* 正确但性能普通的写法

Testing: [ConcurrentHashMapPerformance#normal](./coding-concurrent-tools-concurrent-hash-map/src/test/java/org/geektime/java/common/mistakes/coding/concurrent/tools/concurrent/hash/map/ConcurrentHashMapPerformance.java#L41)

* 推荐写法

Testing: [ConcurrentHashMapPerformance#good](./coding-concurrent-tools-concurrent-hash-map/src/test/java/org/geektime/java/common/mistakes/coding/concurrent/tools/concurrent/hash/map/ConcurrentHashMapPerformance.java#L71)

* 两种写法性能对比

Testing: [ConcurrentHashMapPerformance#benchmark](./coding-concurrent-tools-concurrent-hash-map/src/test/java/org/geektime/java/common/mistakes/coding/concurrent/tools/concurrent/hash/map/ConcurrentHashMapPerformance.java#L94)

#### 在频繁修改数据地场景下使用 CopyOnWriteArrayList 导致性能问题

* 一般来说，针对通用场景的通用解决方案，在所有场景下性能都还可以，属于“万金油”；而针对特殊场景的特殊实现，会有比通用解决方案更高的性能，但一定要在它针对的场景下使用，否则可能会产生性能问题甚至是 Bug。
* CopyOnWriteArrayLis 每次修改数据时都会复制一份数据出来，所以有明显的适用场景，即读多写少或者说希望无锁读的场景。
* 工具/方案的选择一定要是因为场景需要而不是因为足够酷炫

##### 场景

比较并发读写时 CopyOnWriteArrayList 和通用方案 synchronized + ArrayList 的性能

* 并发写性能对比

Testing: [CopyOnWriteArrayListPerformance#benchmarkWrite](./coding-concurrent-tools-copy-on-write-array-list/src/test/java/org/geektime/java/common/mistakes/coding/concurrent/tools/copy/on/write/array/list/CopyOnWriteArrayListPerformance.java#31)

* 并发读性能对比

Testing: [CopyOnWriteArrayListPerformance#benchmarkRead](./coding-concurrent-tools-copy-on-write-array-list/src/test/java/org/geektime/java/common/mistakes/coding/concurrent/tools/copy/on/write/array/list/CopyOnWriteArrayListPerformance.java#56)

#### 比较 ConcurrentHashMap 的 putIfAbsent 和 computeIfAbsent 方法

* putIfAbsent 不允许设置为 null ，computeIfAbsent 计算结果允许为 null，但不会设置 KV 
* 当 key 原本不存在、两者都成功设置 KV时，putIfAbsent 不会返回设置的值，而是返回 null ，要注意后续操作防止 NPE；computeIfAbsent 返回计算结果
* putIfAbsent 总是要计算 value，而 computeIfAbsent 只会在 key 不存在时计算，对计算 value 比较昂贵的情景，这点要特别注意

Testing: [ConcurrentHashMapPiaVsCia#test](./coding-concurrent-tools-concurrent-hash-map/src/test/java/org/geektime/java/common/mistakes/coding/concurrent/tools/concurrent/hash/map/ConcurrentHashMapPiaVsCia.java#L35)

### 代码加锁问题

#### 锁定范围错误一

对共享数据的非原子性的修改操作要上锁，对其读取过程通常也需要上锁，尤其是当将其作为竞态条件时一定读取也要加锁！

* 错误示例

Code: [Interesting#compare](./coding-lock/src/main/java/org/geektime/java/common/mistakes/coding/lock/Interesting.java#L33)
Testing: [InterestingTest#wrong](./coding-lock/src/test/java/org/geektime/java/common/mistakes/coding/lock/InterestingTest.java#L18)

* 正确示例

Code: [Interesting#compareRight](./coding-lock/src/main/java/org/geektime/java/common/mistakes/coding/lock/Interesting.java#L48)
Testing: [InterestingTest#right](./coding-lock/src/test/java/org/geektime/java/common/mistakes/coding/lock/InterestingTest.java#L40)

#### 锁定范围错误二

锁对象要与要锁定的资源切实对应。不要拿自家锁锁别家门。

* 错误示例

Code: [Data#addWrong](./coding-lock/src/main/java/org/geektime/java/common/mistakes/coding/lock/Data.java#L21)
Testing: [DataTest#wrong](./coding-lock/src/test/java/org/geektime/java/common/mistakes/coding/lock/DataTest.java#L26)

* 正确示例

Code: [Data#addRight](./coding-lock/src/main/java/org/geektime/java/common/mistakes/coding/lock/Data.java#L34)
Testing: [DataTest#right](./coding-lock/src/test/java/org/geektime/java/common/mistakes/coding/lock/DataTest.java#L34)
