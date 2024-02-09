package open.demo.task2.dao;

import open.demo.common.pojo.People;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface Task2Dao {

    List<People> getAllPeople();

    People getPeopleById(Integer id);
}
