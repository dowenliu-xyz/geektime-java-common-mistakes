# 在Web容器环境 ThreadLocal 误用问题

* 不能认为没有显式开启多线程就不会有线程安全问题。
* 在 Tomcat 这种 Web 服务器下跑的业务代码，本来就运行在一个多线程环境。
* Tomcat 的工作线程是基于线程池的。
* 线程池会重用固定的几个线程，一旦线程重用，那么很可能首次从 ThreadLocal 获取的值是之前其他线程遗留的值。
* 使用类似 ThreadLocal 工具来存放一些数据时，需要特别注意在代码运行完后，显式地去清空设置的数据。

## Mistake usage

Coding: [ThreadLocalMisuseController#wrong](./src/main/java/org/geektime/java/common/mistakes/coding/concurrent/tools/thread/local/ThreadLocalMisuseController.java#L27)
Testing: [ThreadLocalMisuseControllerTest#wrong](./src/test/java/org/geektime/java/common/mistakes/coding/concurrent/tools/thread/local/ThreadLocalMisuseControllerTest.java#L27)

## Correct usage

Coding: [ThreadLocalMisuseController#right](./src/main/java/org/geektime/java/common/mistakes/coding/concurrent/tools/thread/local/ThreadLocalMisuseController.java#L38)
Testing: [ThreadLocalMisuseControllerTest#right](./src/test/java/org/geektime/java/common/mistakes/coding/concurrent/tools/thread/local/ThreadLocalMisuseControllerTest.java#L55)
