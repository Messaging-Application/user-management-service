package ma.messaging.usermanagementservice.service;

import java.util.Map;

public interface RedisService {
    void storeKeyValuePair(String key, String value);
    String getValueByKey(String key);
    Map<String, String> getAllKeyValuePairs();
}