package open.demo.task3.service;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import open.demo.common.pojo.People;
import open.demo.rocketmq.RocketMQProducer;
import open.demo.task3.dao.Task3Dao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class Task3Service {
    @Resource
    private Task3Dao task3Dao;

    private final ThreadPoolExecutor executorWithMessage;

    private final ThreadPoolExecutor executorWithoutMessage;

    @Autowired(required = false)
    private RocketMQProducer producer;

    @Data
    @AllArgsConstructor
    class PeopleTask implements Runnable {
        private People people;

        @Override
        public void run() {
            Task3Service.this.task1(people);
        }
    }

    //初始化两个线程池
    public Task3Service() {
        executorWithoutMessage = new ThreadPoolExecutor(12, 24, 60,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(10000), new ThreadPoolExecutor.AbortPolicy());
        executorWithMessage = new ThreadPoolExecutor(12, 24, 60,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(10000), (r, executor) -> {
            log.warn("线程池触发拒绝策略");
            producer.sendMessage("demo54", ((PeopleTask) r).getPeople());
        });
    }
    //直接写数据库
    public void task1(People people) {
        task3Dao.insertPeople(people);
    }

    //不搭配消息队列，直接快速失败
    public void task2(People people) {
        executorWithoutMessage.execute(new PeopleTask(people));
    }
    //失败后使用消息队列
    public void task3(People people) {
        executorWithMessage.execute(new PeopleTask(people));
    }
}
