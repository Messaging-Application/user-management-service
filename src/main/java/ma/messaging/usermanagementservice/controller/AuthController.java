package ma.messaging.usermanagementservice.controller;

import jakarta.validation.Valid;
import ma.messaging.usermanagementservice.payload.requests.EditRequest;
import ma.messaging.usermanagementservice.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestParam;

@CrossOrigin(origins = "http://localhost:5173", maxAge = 3600, allowCredentials = "true")
@RestController
@RequestMapping("/user")
public class AuthController {

    private final AccountService accountService;

    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PatchMapping("/edit/{id}")
    public ResponseEntity<?> userEdit(@PathVariable("id") int id, @Valid @RequestBody EditRequest request, @CookieValue(name = "${application.security.jwt.cookie-name}", required = true) String jwtToken) {
        return accountService.userEdit(id, request, jwtToken);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> userDelete(@PathVariable("id") int id, @CookieValue(name = "${application.security.jwt.cookie-name}", required = true) String jwtToken) {
        return accountService.userDelete(id, jwtToken);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable("id") int id, @CookieValue(name = "${application.security.jwt.cookie-name}", required = true) String jwtToken) {
        return accountService.getUser(id, jwtToken);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getUsers(@CookieValue(name = "${application.security.jwt.cookie-name}", required = true) String jwtToken, @RequestParam(defaultValue = "0") int pageNo, @RequestParam(defaultValue = "3") int pageSize) {
        return accountService.getUsers(jwtToken, pageNo, pageSize);
    }
}
