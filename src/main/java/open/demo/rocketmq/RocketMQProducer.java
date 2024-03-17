package open.demo.rocketmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class RocketMQProducer {
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private ObjectMapper mapper;

    // 同步发送消息
    public void sendMessage(String topic, Object msg) {
        try {
            String s = mapper.writeValueAsString(msg);
            log.info("发送消息,topic:{},msg:{}", topic, s);
            rocketMQTemplate.convertAndSend(topic, s);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
