package open.demo;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

//-Xmx256m -Xms256m
//-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=C:/Users/SongJi/Desktop
@SpringBootApplication
public class Demo54 {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(Demo54.class, args);
    }

}
