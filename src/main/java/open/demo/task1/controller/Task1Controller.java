package open.demo.task1.controller;

import open.demo.task1.service.Task1Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/task1")
public class Task1Controller {
    @Resource
    private Task1Service service;

    /**
     * 接口1：空接口
     * 任务1：要求使用jmeter测试接口的性能，并用arthas对接口做简单的trace调用做简单分析
     * 任务2：使用jmeter进行接口性能测试，使用arthas的watch或者trace监控那些RT异常点
     * watch open.demo.task1.controller.Task1Controller api1 "{params, returnObj}" "#cost>1" -n 1000000
     * trace open.demo.task1.controller.Task1Controller api1 "#cost>1" -n 1000000
     */
    @RequestMapping(value = "/api1")
    public String api1() {
        return "hello";
    }

    /**
     * 接口2：num%10=1时会导致接口阻塞50ms
     * 任务1：使用jmeter测试该接口性能，吞吐量：1991，最大值：91，最小值：0
     * 任务2：使用jmeter测试跳过num%10=1的参数值，吞吐量：9747，最大值：35，最小值：0
     * 任务3：使用arthas定位耗时部位
     * -> trace open.demo.task1.controller.Task1Controller api2 "#cost>50"
     * -> watch open.demo.task1.controller.Task1Controller api2 "{params, returnObj}" "#cost>50" -x 2
     */
    @RequestMapping(value = "/api2/{num}")
    public String api2(@PathVariable(value = "num") Integer num) {
        return service.task2(num);
    }


}
