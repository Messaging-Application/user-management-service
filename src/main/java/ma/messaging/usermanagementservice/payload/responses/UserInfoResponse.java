package ma.messaging.usermanagementservice.payload.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserInfoResponse {
    private int id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> roles;
}
