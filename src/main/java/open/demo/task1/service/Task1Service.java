package open.demo.task1.service;

import org.springframework.stereotype.Service;

@Service
public class Task1Service {

    public String task2(Integer num) {
        if (num % 10 == 1) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return String.format("you number is: %s", num);
    }
}
