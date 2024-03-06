package ma.messaging.usermanagementservice.service;

import redis.clients.jedis.Jedis;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

@Service
public class RedisServiceImpl implements RedisService {

    private final Jedis jedis;

    public RedisServiceImpl(@Value("${spring.redis.host}") String redisHost) {
        this.jedis = new Jedis(redisHost, 6379); // Connect to Redis server
    }

    @Override
    public void storeKeyValuePair(String key, String value) {
        jedis.set(key, value); // Store key-value pair in Redis
    }

    @Override
    public String getValueByKey(String key) {
        return jedis.get(key); // Retrieve value by key from Redis
    }

    @Override
    public Map<String, String> getAllKeyValuePairs() {
        Map<String, String> keyValuePairs = new HashMap<>();
        Set<String> keys = jedis.keys("*"); // Get all keys from Redis

        if (keys != null) {
            for (String key : keys) {
                String value = jedis.get(key);
                keyValuePairs.put(key, value);
            }
        }

        return keyValuePairs;
    }
}
