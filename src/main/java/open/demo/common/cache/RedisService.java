package open.demo.common.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class RedisService {
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    public void set(String key, Object value) {
        if (StringUtils.isEmpty(key) || value == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void set(String key, Object value, long expireTime) {
        if (StringUtils.isEmpty(key) || value == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), expireTime, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T get(String key, Class<T> targetClass) {
        String result = redisTemplate.opsForValue().get(key);
        if (result == null) {
            return null;
        } else {
            try {
                return objectMapper.readValue(result, targetClass);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Boolean remove(String key) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        return redisTemplate.delete(key);
    }

    public Boolean contains(String key) {
        return redisTemplate.hasKey(key);
    }

}
