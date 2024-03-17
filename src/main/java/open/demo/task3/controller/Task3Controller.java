package open.demo.task3.controller;

import open.demo.common.pojo.People;
import open.demo.common.pojo.ResponseBean;
import open.demo.task3.service.Task3Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.UUID;


@RestController
@RequestMapping(value = "/task3")
public class Task3Controller {
    @Resource
    private Task3Service service;

    /**
     * 接口1：直接写数据进mysql
     * 任务1：使用jmeter测试该接口性能（测试要求拉满mysql）
     */
    @RequestMapping(value = "/api1")
    public ResponseBean api1() {
        People people = new People(null, UUID.randomUUID().toString(), 0);
        service.task1(people);
        return ResponseBean.success(people, "");
    }

    /**
     * 基于线程池，写入mysql
     *
     * @return
     */
    @RequestMapping(value = "/api2")
    public ResponseBean api2() {
        People people = new People(null, UUID.randomUUID().toString(), 0);
        service.task2(people);
        return ResponseBean.success(people, "");
    }

    /**
     * 基于线程池搭配消息队列，写入mysql
     */
    @RequestMapping(value = "/api3")
    public ResponseBean api3() {
        People people = new People(null, UUID.randomUUID().toString(), 0);
        service.task3(people);
        return ResponseBean.success(people, "");
    }

}
