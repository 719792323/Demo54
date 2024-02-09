package open.demo.task2.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import open.demo.common.cache.RedisService;
import open.demo.common.pojo.People;
import open.demo.task2.dao.Task2Dao;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class Task2Service {
    @Resource
    private Task2Dao dao;

    @Resource
    private RedisService redisService;


    private Cache<String, People> peopleCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .build();

    public List<People> task1() {
        return dao.getAllPeople();
    }

    public People task2(Integer id) {
        return dao.getPeopleById(id);
    }

    public People task3(Integer id) {
        People people = redisService.get(String.valueOf(id), People.class);
        if (people == null) {
            people = task2(id);
            redisService.set(String.valueOf(id), people == null ? People.NULL : people, 60);
        }
        return people;
    }


    public People task4(Integer id) {
        People people = peopleCache.getIfPresent(String.valueOf(id));
        if (people == null) {
            people = task2(id);
            peopleCache.put(String.valueOf(id), people == null ? People.NULL : people);
        }
        return people;
    }
}
