package open.demo.rocketmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import open.demo.common.pojo.People;
import open.demo.task3.service.Task3Service;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@RocketMQMessageListener(consumerGroup = "demo54_consumer1", topic = "demo54")
@Profile("!test")
@Slf4j
public class RocketMQConsumer implements RocketMQListener<String> {

    @Resource
    private Task3Service service;

    @Resource
    private ObjectMapper mapper;

    @Override
    public void onMessage(String s) {
        try {
            log.info("收到消息:{}", s);
            service.task3(mapper.readValue(s, People.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
