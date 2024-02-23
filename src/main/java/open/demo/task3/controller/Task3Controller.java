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
     * 任务1：使用jmeter测试该接口性能（测试要求拉满mysql），吞吐量：557，请求总数：50148，平均延迟：43
     */
    @RequestMapping(value = "/api1")
    public ResponseBean api1() {
        People people = new People(null, UUID.randomUUID().toString(), 0);
        service.task1(people);
        return ResponseBean.success(people, "");
    }

}
