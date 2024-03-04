package ma.messaging.usermanagementservice.service;

import lombok.RequiredArgsConstructor;
import ma.messaging.usermanagementservice.model.Account;
import ma.messaging.usermanagementservice.model.ERole;
import ma.messaging.usermanagementservice.model.Role;
import ma.messaging.usermanagementservice.payload.requests.EditRequest;
import ma.messaging.usermanagementservice.payload.responses.MessageResponse;
import ma.messaging.usermanagementservice.payload.responses.UserInfoResponse;
import ma.messaging.usermanagementservice.repository.AccountRepository;
import ma.messaging.usermanagementservice.repository.RoleRepository;
import ma.messaging.usermanagementservice.security.jwt.JwtUtils;
import ma.messaging.usermanagementservice.security.services.UserDetailsImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.CookieValue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    public ResponseEntity<?> userEdit(@PathVariable("id") int id, EditRequest request, @CookieValue(name = "${application.security.jwt.cookie-name}", required = true) String jwtToken) {
        String usernameFromJwt = jwtUtils.getUserNameFromJwtToken(jwtToken);
        Account userRequesting = accountRepository.findByUsername(usernameFromJwt)
                                                  .orElseThrow(() -> new RuntimeException("Error: User is not found."));
        Account oldAccount = accountRepository.findById(id)
                                        .orElseThrow(() -> new RuntimeException("Error: User is not found."));
        String usernameFromId = oldAccount.getUsername();

        boolean isAdmin = userRequesting.getRoles().stream().anyMatch(role -> role.getName().equals("admin"));

        if (!usernameFromJwt.equals(usernameFromId) && isAdmin == false) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to edit this item.");
        }

        String newUsername = request.getUsername();
        String newEmail = request.getEmail();
        String newFirstName = request.getFirstName();
        String newLastName = request.getLastName();
        String newPassword = request.getPassword();

        if (newUsername != null) {
            if (accountRepository.existsByUsername(newUsername) && !newUsername.equals(oldAccount.getUsername())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
            }
            oldAccount.setUsername(newUsername);
        }

        if (newEmail != null) {
            if (accountRepository.existsByEmail(newEmail) && !newEmail.equals(oldAccount.getEmail())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already taken!"));
            }
            oldAccount.setEmail(newEmail);
        }

        if (newFirstName != null) {
            oldAccount.setFirstName(newFirstName);
        }

        if (newLastName != null) {
            oldAccount.setLastName(newLastName);
        }

        if (newPassword != null) {
            oldAccount.setPassword(passwordEncoder.encode(newPassword));
        }

        accountRepository.save(oldAccount);
        return ResponseEntity.ok(new MessageResponse("User data updated successfully!"));
    }

    @Override
    public ResponseEntity<?> userDelete(@PathVariable("id") int id, @CookieValue(name = "${application.security.jwt.cookie-name}", required = true) String jwtToken) {
        String usernameFromJwt = jwtUtils.getUserNameFromJwtToken(jwtToken);
        Account userRequesting = accountRepository.findByUsername(usernameFromJwt)
                                                  .orElseThrow(() -> new RuntimeException("Error: User is not found."));
        Account user = accountRepository.findById(id)
                                        .orElseThrow(() -> new RuntimeException("Error: User is not found."));
        String usernameFromId = user.getUsername();

        boolean isAdmin = userRequesting.getRoles().stream().anyMatch(role -> role.getName().equals("admin"));

        if (!usernameFromJwt.equals(usernameFromId) && isAdmin == false) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this item.");
        }
        accountRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully!"));
    }

    @Override
    public ResponseEntity<?> getUser(@PathVariable("id") int id, @CookieValue(name = "${application.security.jwt.cookie-name}", required = true) String jwtToken) {
        String usernameFromJwt = jwtUtils.getUserNameFromJwtToken(jwtToken);
        Account userRequesting = accountRepository.findByUsername(usernameFromJwt)
                                                  .orElseThrow(() -> new RuntimeException("Error: User is not found."));
        Account user = accountRepository.findById(id)
                                        .orElseThrow(() -> new RuntimeException("Error: User is not found."));
        String usernameFromId = user.getUsername();

        boolean isAdmin = userRequesting.getRoles().stream().anyMatch(role -> role.getName().equals("admin"));

        if (!usernameFromJwt.equals(usernameFromId) && isAdmin == false) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to see this item.");
        }
        user.setPassword("");
        return ResponseEntity.ok(user);
    }
}

