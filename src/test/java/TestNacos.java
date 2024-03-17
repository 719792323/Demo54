
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import open.demo.Demo54;
import open.demo.common.pojo.People;
import open.demo.nacos.NacosMain;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.locks.LockSupport;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Demo54.class, args = {"--spring.profiles.active=local"})
public class TestNacos {
    @Autowired
    private NacosMain nacosMain;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void testGetConfig() throws JsonProcessingException {
        String config = nacosMain.getConfig();
        Assert.assertNotNull(config);
        People people = mapper.readValue(config, People.class);
        Assert.assertNotNull(people);
        System.out.println(people);
    }

    @Test
    public void testListenConfig() throws Exception {
        String config1 = nacosMain.getConfig();
        People people1 = mapper.readValue(config1, People.class);
        System.out.println(people1);
        //监听变化，需要对配置中的年龄修改+1
        Future<String> future = nacosMain.listenConfig();
        String config2 = future.get();
        People people2 = mapper.readValue(config2, People.class);
        System.out.println(people2);
        Assert.assertEquals(Integer.valueOf(people1.getAge() + 1), people2.getAge());
    }

    @Test
    public void testRegisterAndGetServer() throws Exception {
        String serverName = "demo54-nacos";
        String ip = "127.0.0.1";
        int port = 8080;
        nacosMain.registerServer(serverName, ip, port);
        Thread.sleep(500);
        List<Instance> demo54 = nacosMain.getServer(serverName);
        Assert.assertFalse(demo54.isEmpty());
        System.out.println(demo54);
        Instance instance = demo54.get(0);
        Assert.assertEquals(ip, instance.getIp());
        Assert.assertEquals(port, instance.getPort());
    }

    @Test
    public void testListenServer() throws Exception {
        String serverName = "demo54-nacos";
        String ip = "127.0.0.1";
        int port = 8080;
        Future<List<Instance>> listFuture = nacosMain.listenServers(serverName);
        nacosMain.registerServer(serverName, ip, port);
        List<Instance> instances = listFuture.get();
        System.out.println(instances);
        Assert.assertFalse(instances.isEmpty());
        Instance instance = instances.get(0);
        Assert.assertEquals(ip, instance.getIp());
        Assert.assertEquals(port, instance.getPort());
    }
}
