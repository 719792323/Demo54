package open.demo.threadpool;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
class OOMRejectedPolicy implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        log.debug("触发OOM溢出策略");
    }
}

@Slf4j
class NOOOMRejectedPolicy implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        log.debug("触发溢出策略，并抛出异常");
        throw new RuntimeException("reject policy");

    }
}

class OOMThreadFactory implements ThreadFactory {
    private AtomicInteger id = new AtomicInteger(0);

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, String.format("oom-pool-thread-%s", id.getAndIncrement()));
    }
}

class OOMResult {
    public int id;

    public OOMResult(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "OOMResult{" + "id=" + id + '}';
    }
}

//设置堆内存：-Xmx10m -Xms10m
//设置OOM后DUM文件，-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=C:/Users/SongJi/Desktop
//jvisualvm.exe dump文件分析
//jconsole
public class ThreadPoolMain {
    static Map<Integer, Future<OOMResult>> futureTaskMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        ThreadPoolExecutor oomThreadPool = new ThreadPoolExecutor(1, 1, 60,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(1), new OOMRejectedPolicy());
        AtomicInteger ids = new AtomicInteger(0);
        Thread submitThread = new Thread(() -> {
            while (true) {
                try {
                    for (int i = 0; i < 100; i++) {
                        int id = ids.getAndIncrement();
                        Future<OOMResult> submit = oomThreadPool.submit(() -> new OOMResult(id));
                        futureTaskMap.put(id, submit);
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }
        }, "submit-thread");
        Thread saveThread = new Thread(() -> {
            while (true) {
                try {
                    System.out.println(String.format("futureTaskMap size:%s", futureTaskMap.size()));
                    Iterator<Integer> iterator = futureTaskMap.keySet().iterator();
                    while (iterator.hasNext()) {
                        Integer key = iterator.next();
                        Future<OOMResult> future = futureTaskMap.get(key);
                        if (future.isDone() || future.isCancelled()) {
                            if (future.isDone()) {
                                OOMResult oomResult = future.get();
                            }
                            futureTaskMap.remove(key);
                        }
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "saveThread");
        submitThread.start();
        saveThread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> oomThreadPool.shutdownNow()));
    }

}
