package open.demo.task3.service;


import open.demo.common.pojo.People;
import open.demo.task3.dao.Task3Dao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class Task3Service {
    @Resource
    private Task3Dao task3Dao;

    public void task1(People people) {
        task3Dao.insertPeople(people);
    }
}
