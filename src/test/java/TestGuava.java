import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import open.demo.Demo54;
import open.demo.common.pojo.People;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Demo54.class, args = {"--spring.profiles.active=local"})
public class TestGuava {

    Cache<String, Object> cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .build();

    @Test
    public void testSet() {
        People people = new People(1, "sj", 10);
        cache.put("1", people);
        Assert.assertEquals(people, cache.getIfPresent("1"));
    }

}
