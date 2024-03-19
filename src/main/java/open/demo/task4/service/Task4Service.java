package open.demo.task4.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class Task4Service {
    private Map<Integer, Future<Result>> futureTaskMap = new ConcurrentHashMap<>();
    private ThreadPoolExecutor oomThreadPool = new ThreadPoolExecutor(1, 1, 60,
            TimeUnit.SECONDS, new ArrayBlockingQueue<>(100), new OOMRejectedPolicy());
    private ThreadPoolExecutor noOOMThreadPool = new ThreadPoolExecutor(1, 1, 60,
            TimeUnit.SECONDS, new ArrayBlockingQueue<>(100), new NOOOMRejectedPolicy());
    private AtomicInteger ids = new AtomicInteger();


    Thread saveThread = new Thread(() -> {
        while (true) {
            try {
//                log.info(String.format("futureTaskMap size:%s", futureTaskMap.size()));
                Iterator<Integer> iterator = futureTaskMap.keySet().iterator();
                while (iterator.hasNext()) {
                    Integer key = iterator.next();
                    Future<Result> future = futureTaskMap.get(key);
                    if (future.isDone() || future.isCancelled()) {
                        if (future.isDone()) {
                            future.get();
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

    public Task4Service() {
        saveThread.start();
    }

    public void task1() {
        int id = ids.getAndIncrement();
        Future<Result> submit = oomThreadPool.submit(() -> new Result(id));
        futureTaskMap.put(id, submit);
    }

    public void task2() {
        int id = ids.getAndIncrement();
        try {
            Future<Result> submit = noOOMThreadPool.submit(() -> new Result(id));
            futureTaskMap.put(id, submit);
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    //用于测试生成uuid速度接口
    public void task3() {
        String s = UUID.randomUUID().toString();
    }

}

class Result {
    public int id;

    public Result(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Result{" + "id=" + id + '}';
    }
}

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
