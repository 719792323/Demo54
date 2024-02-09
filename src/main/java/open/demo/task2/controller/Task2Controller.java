package open.demo.task2.controller;

import open.demo.common.pojo.People;
import open.demo.common.pojo.ResponseBean;
import open.demo.task2.service.Task2Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping(value = "/task2")
public class Task2Controller {
    @Resource
    private Task2Service service;

    /**
     * 接口1：查询数据接口
     * 任务1：使用jmeter测试该接口性能，吞吐量：3431，最大延迟：79，最小：1
     * 任务2：trace该接口执行时间
     * trace open.demo.task2.controller.Task2Controller api1
     * `---[3.9843ms] open.demo.task2.controller.Task2Controller:api1()
     * +---[92.63% 3.6908ms ] open.demo.task2.service.Task2Service:task1() #20
     * `---[0.21% 0.0083ms ] open.demo.common.pojo.ResponseBean:success() #21
     * 任务3：使用javaagent监控这个基于Hikari连接池对应的explain数据
     * -javaagent:xx.jar
     */
    @RequestMapping(value = "/api1")
    public ResponseBean api1() {
        List<People> peoples = service.task1();
        return ResponseBean.success(peoples, "success");
    }

    /**
     * 接口2：根据id查询数据接口，直通mysql
     * 任务1：使用jmeter测试该接口性能，吞吐量：4221，平均值：2
     * 任务2：部署各类exporter监控中间件资源使用情况
     */
    @RequestMapping(value = "/api2/{id}")
    public ResponseBean api2(@PathVariable("id") Integer id) {
        People people = service.task2(id);
        return ResponseBean.success(people, "success");
    }

    /**
     * 接口3：根据id查询数据接口，mysql搭配redis
     * 任务1：使用jmeter测试该接口性能，吞吐量：5658，平均值：1
     * 任务2：trace走mysql和走redis速度区别
     * -->走mysql
     * ClassLoader@7a94b64e
     * `---[5.5668ms] open.demo.task2.service.Task2Service:task3()
     * +---[24.20% 1.3472ms ] open.demo.common.cache.RedisService:get() #29
     * +---[52.89% 2.9442ms ] open.demo.task2.service.Task2Service:task2() #31 //mysql
     * `---[21.08% 1.1733ms ] open.demo.common.cache.RedisService:set() #32
     * -->走redis
     * ClassLoader@7a94b64e
     * `---[1.2832ms] open.demo.task2.service.Task2Service:task3()
     * `---[97.85% 1.2556ms ] open.demo.common.cache.RedisService:get() #29
     * 任务3：部署各类exporter监控中间件资源使用情况
     */
    @RequestMapping(value = "/api3/{id}")
    public ResponseBean api3(@PathVariable("id") Integer id) {
        People people = service.task3(id);
        return ResponseBean.success(people, "success");
    }

    /**
     * 接口4：根据id查询数据接口，mysql搭配guava
     * 任务1：使用jmeter测试该接口性能，吞吐量：9043，平均值：1
     */
    @RequestMapping(value = "/api4/{id}")
    public ResponseBean api4(@PathVariable("id") Integer id) {
        People people = service.task4(id);
        return ResponseBean.success(people, "success");
    }
}
