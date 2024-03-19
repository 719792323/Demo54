package open.demo.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

@Component
@Profile("skywalking")
public class NacosMain {
    @Value("${nacos.serverAddr}")
    private String serverAddr;
    @Value("${nacos.dataId}")
    private String dataId;
    @Value("${nacos.group}")
    private String group;

    //获取配置
    public String getConfig() {
        try {
            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);
            ConfigService configService = NacosFactory.createConfigService(properties);
            String content = configService.getConfig(dataId, group, 5000);
            return content;
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    ThreadLocal<String> listenConfigLocal = new ThreadLocal<>();

    //监听配置
    public Future<String> listenConfig() throws Exception {
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        ConfigService configService = NacosFactory.createConfigService(properties);
        FutureTask<String> configTask = new FutureTask<>(() -> listenConfigLocal.get());
        //注册监听
        configService.addListener(dataId, group, new Listener() {
            @Override
            public void receiveConfigInfo(String configInfo) {
                listenConfigLocal.set(configInfo);
                configTask.run();
                listenConfigLocal.remove();
                //删除监听
                configService.removeListener(dataId, group, this);
            }

            @Override
            public Executor getExecutor() {
                return null;
            }
        });
        return configTask;
    }

    //注册server，当本服务如果掉线了，注册的服务会自动失效（基于心跳机制）
    public void registerServer(String serverName, String ip, int port) throws Exception {
        NamingService naming = NamingFactory.createNamingService(serverAddr);
        naming.registerInstance(serverName, ip, port);
    }

    //获取指定名称的server
    public List<Instance> getServer(String serverName) throws Exception {
        NamingService naming = NamingFactory.createNamingService(serverAddr);
        List<Instance> instances = naming.selectInstances(serverName, true);
        return instances;
    }

    ThreadLocal<List<Instance>> listenServerLocal = new ThreadLocal<>();

    //监听server
    public Future<List<Instance>> listenServers(String serverName) throws Exception {
        NamingService naming = NamingFactory.createNamingService(serverAddr);
        FutureTask<List<Instance>> instanceTask = new FutureTask<>(() -> listenServerLocal.get());
        naming.subscribe(serverName, new EventListener() {
            @Override
            public void onEvent(Event event) {
                if (event instanceof NamingEvent && !((NamingEvent) event).getInstances().isEmpty()) {
                    listenServerLocal.set(((NamingEvent) event).getInstances());
                    instanceTask.run();
                    listenServerLocal.remove();
                    try {
                        naming.unsubscribe(serverName, this);
                    } catch (NacosException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        return instanceTask;
    }


}
