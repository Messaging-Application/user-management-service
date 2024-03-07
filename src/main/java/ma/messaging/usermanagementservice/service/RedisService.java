package ma.messaging.usermanagementservice.service;

import ma.messaging.usermanagementservice.model.Account;
import java.util.Map;

public interface RedisService {
    void storeKeyValuePair(String key, String value);
    String getValueByKey(String key);
    Map<String, String> getAllKeyValuePairs(Account user);
    String generateChatId(String userId1, String userId2);
    boolean isUserPairInRedis(String userId1, String userId2);
    boolean exists(String key);
    void deleteUserFromRedis(Account user);
    void editUserInRedis(Account user);
}