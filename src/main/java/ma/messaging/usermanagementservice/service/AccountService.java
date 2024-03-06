package ma.messaging.usermanagementservice.service;

import ma.messaging.usermanagementservice.payload.requests.EditRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.CookieValue;

public interface AccountService {
    ResponseEntity<?> userEdit(@PathVariable("id") int id, EditRequest request, @CookieValue(name = "${application.security.jwt.cookie-name}", required = true) String jwtToken);
    
    ResponseEntity<?> userDelete(@PathVariable("id") int id, @CookieValue(name = "${application.security.jwt.cookie-name}", required = true) String jwtToken);

    ResponseEntity<?> getUser(@PathVariable("id") int id, @CookieValue(name = "${application.security.jwt.cookie-name}", required = true) String jwtToken);

    ResponseEntity<?> getUsers(@CookieValue(name = "${application.security.jwt.cookie-name}", required = true) String jwtToken, int pageNo, int pageSize);
}
