import com.fasterxml.jackson.databind.ObjectMapper;
import open.demo.Demo54;
import open.demo.common.pojo.People;
import open.demo.rocketmq.RocketMQProducer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.SynchronousQueue;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = Demo54.class, args = {"--spring.profiles.active=local"})
//@ActiveProfiles("test")
public class TestRocketMQ {
//    @Autowired
    private RocketMQProducer producer;

    private DefaultMQPushConsumer consumer;

//    @Value("${rocketmq.name-server}")
    private String nameServer;

    private SynchronousQueue<List<MessageExt>> syncQueue = new SynchronousQueue<>();

//    @Autowired
    private ObjectMapper mapper;

    @Before
    public void init() throws Exception {
        consumer = new DefaultMQPushConsumer();
        consumer.setNamesrvAddr(nameServer);
        consumer.setConsumerGroup("demo54_consumer1");
        consumer.subscribe("demo54", "*");
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                try {
                    syncQueue.put(list);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
    }

    @Test
    public void testProduceAndConsumeMessage() throws Exception {
        People sj = new People(1, "sj", 10);
        producer.sendMessage("demo54", sj);
        List<MessageExt> message = syncQueue.take();
        System.out.println(message);
        Assert.assertFalse(message.isEmpty());
        People sj2 = mapper.readValue(new String(message.get(0).getBody()), People.class);
        Assert.assertEquals(sj, sj2);
    }
}

