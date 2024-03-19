package open.demo.proto;

import com.fasterxml.jackson.databind.ObjectMapper;
import open.demo.common.pojo.People;
import open.demo.common.pojo.PeopleProto;

import java.io.ByteArrayOutputStream;

public class ProtoMain {
    public static void main(String[] args) throws Exception {
        People people = new People(1, "sj", 10, "13870724913");
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(people);
        System.out.println(json);
        double len1 = json.getBytes().length;
        PeopleProto.People protoPeople = PeopleProto.People.newBuilder()
                .setId(people.getId())
                .setName(people.getName())
                .setAge(people.getAge())
                .setPhone(people.getPhone()).build();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        protoPeople.writeTo(outputStream);
        double len2 = outputStream.toByteArray().length;
        System.out.println(String.format("len1:%s,len2:%s,len1/len2:%s", len1, len2, len1 / len2));
        PeopleProto.People parseFrom = PeopleProto.People.parseFrom(outputStream.toByteArray());
        System.out.println(parseFrom);
    }
}
