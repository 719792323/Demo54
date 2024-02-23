import open.demo.Demo54;
import open.demo.common.pojo.People;
import open.demo.task3.dao.Task3Dao;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Demo54.class, args = {"--spring.profiles.active=local"})
public class TestTask3Dao {
    @Resource
    private Task3Dao task3Dao;

    @Test
    public void testInsertPeople() {
        boolean success = task3Dao.insertPeople(new People(null, UUID.randomUUID().toString(), 10));
        Assert.assertTrue(success);
    }
}
