import com.fasterxml.jackson.databind.ObjectMapper;
import open.demo.Demo54;
import open.demo.common.pojo.People;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Demo54.class, args = {"--spring.profiles.active=local"})
public class TestJackson {
    @Resource
    private ObjectMapper mapper;

    @Test
    public void test1() throws Exception {
        People sj = new People(1, "sj", 10, null);
        Assert.assertEquals("{\"id\":1,\"name\":\"sj\",\"age\":10}", mapper.writeValueAsString(sj));
    }

    @Test
    public void test2() throws Exception {
        People sj = new People(1, "sj", 10, null);
        String json = mapper.writeValueAsString(sj);
        People people = mapper.readValue(json, People.class);
        Assert.assertEquals(sj, people);
    }
}
