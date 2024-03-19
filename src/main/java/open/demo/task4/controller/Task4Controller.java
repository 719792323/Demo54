package open.demo.task4.controller;

import open.demo.common.pojo.ResponseBean;
import open.demo.task4.service.Task4Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/task4")
public class Task4Controller {
    @Resource
    private Task4Service service;

    @RequestMapping(value = "/api1")
    public ResponseBean api1() {
        service.task1();
        return ResponseBean.success(null, "");
    }

    @RequestMapping(value = "/api2")
    public ResponseBean api2() {
        service.task2();
        return ResponseBean.success(null, "");
    }

    @RequestMapping(value = "/api3")
    public ResponseBean api3() {
        service.task3();
        return ResponseBean.success(null, "");
    }
}
