package ma.messaging.usermanagementservice.service;

import redis.clients.jedis.Jedis;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import ma.messaging.usermanagementservice.model.Account;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
    public boolean exists(String key) {
        return jedis.exists(key);
    }


    @Override
    public Map<String, String> getAllKeyValuePairs(Account user) {
        Map<String, String> keyValuePairs = new HashMap<>();
        Set<String> keys = jedis.keys("*"); // Get all keys from Redis

        if (keys != null) {
            for (String key : keys) {
                String value = jedis.get(key);
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(value, JsonObject.class);
                int user1Id = jsonObject.getAsJsonObject("user1").get("id").getAsInt();
                if (user1Id == user.getId()) {
                    keyValuePairs.put(key, value);
                }
            }
        }
        return keyValuePairs;
    }

    @Override
    public String generateChatId(String userId1, String userId2) {
        String concatenatedIds = userId1 + "_" + userId2;
        UUID uuid = UUID.nameUUIDFromBytes(concatenatedIds.getBytes());
        return uuid.toString();
    }

    @Override
    public boolean isUserPairInRedis(String userId1, String userId2) {
        String uniqueId = generateChatId(userId1, userId2);
        String redisKey = "chat:" + uniqueId;
        return exists(redisKey);
    }

    @Override
    public void deleteUserFromRedis(Account user) {
        Set<String> keys = jedis.keys("*"); 
        if (keys != null) {
            for (String key : keys) {
                String value = jedis.get(key);
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(value, JsonObject.class);

                int user1Id = jsonObject.getAsJsonObject("user1").get("id").getAsInt();
                int user2Id = jsonObject.getAsJsonObject("user2").get("id").getAsInt();

                if (user1Id == user.getId() || user2Id == user.getId()) {
                    jedis.del(key);
                }
            }
        }
    }

    @Override
    public void editUserInRedis(Account user) {
        Set<String> keys = jedis.keys("*"); 
        if (keys != null) {
            for (String key : keys) {
                String value = jedis.get(key);
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(value, JsonObject.class);

                int user1Id = jsonObject.getAsJsonObject("user1").get("id").getAsInt();
                int user2Id = jsonObject.getAsJsonObject("user2").get("id").getAsInt();

                if (user1Id == user.getId()) {
                    String newValue = "{\"user1\": {" +
                    "\"username\": \"" + user.getUsername() + "\", " +
                    "\"id\": \"" + user.getId() + "\", " +
                    "\"email\": \"" + user.getEmail() + "\", " +
                    "\"lastName\": \"" + user.getLastName() + "\", " +
                    "\"firstName\": \"" + user.getFirstName() + "\"}, " +
                    "\"user2\": {" +
                    "\"username\": \"" + jsonObject.getAsJsonObject("user2").get("username").getAsString() + "\", " +
                    "\"id\": \"" + jsonObject.getAsJsonObject("user2").get("id").getAsInt() + "\", " +
                    "\"email\": \"" + jsonObject.getAsJsonObject("user2").get("email").getAsString() + "\", " +
                    "\"lastName\": \"" + jsonObject.getAsJsonObject("user2").get("lastName").getAsString() + "\", " +
                    "\"firstName\": \"" + jsonObject.getAsJsonObject("user2").get("firstName").getAsString() + "\"}}";
                    storeKeyValuePair(key, newValue);
                } else if (user2Id == user.getId()) {
                    String newValue = "{\"user1\": {" +
                    "\"username\": \"" + jsonObject.getAsJsonObject("user1").get("username").getAsString() + "\", " +
                    "\"id\": \"" + jsonObject.getAsJsonObject("user1").get("id").getAsInt() + "\", " +
                    "\"email\": \"" + jsonObject.getAsJsonObject("user1").get("email").getAsString() + "\", " +
                    "\"lastName\": \"" + jsonObject.getAsJsonObject("user1").get("lastName").getAsString() + "\", " +
                    "\"firstName\": \"" + jsonObject.getAsJsonObject("user1").get("firstName").getAsString() + "\"}, " +
                    "\"user2\": {" +
                    "\"username\": \"" + user.getUsername() + "\", " +
                    "\"id\": \"" + user.getId() + "\", " +
                    "\"email\": \"" + user.getEmail() + "\", " +
                    "\"lastName\": \"" + user.getLastName() + "\", " +
                    "\"firstName\": \"" + user.getFirstName() + "\"}}";
                    storeKeyValuePair(key, newValue);
                }
            }
        }
    }
}
