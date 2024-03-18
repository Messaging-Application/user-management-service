package ma.messaging.usermanagementservice.payload.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class RedisResponse {
    Map<String, String> redisData;
    int totalPages;
}
