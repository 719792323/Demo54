package open.demo.task3.dao;

import open.demo.common.pojo.People;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface Task3Dao {

    @Insert(value = "INSERT INTO people(name,age) VALUES(#{name},#{age})")
    boolean insertPeople(People people);
}
