package ma.messaging.usermanagementservice.payload.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class EditRequest {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
}
