package ma.messaging.usermanagementservice.service;

import lombok.RequiredArgsConstructor;
import ma.messaging.usermanagementservice.model.Account;
import ma.messaging.usermanagementservice.model.ERole;
import ma.messaging.usermanagementservice.model.Role;
import ma.messaging.usermanagementservice.payload.requests.EditRequest;
import ma.messaging.usermanagementservice.payload.responses.RedisResponse;
import ma.messaging.usermanagementservice.payload.responses.MessageResponse;
import ma.messaging.usermanagementservice.payload.responses.UserInfoResponse;
import ma.messaging.usermanagementservice.repository.AccountRepository;
import ma.messaging.usermanagementservice.repository.RoleRepository;
import ma.messaging.usermanagementservice.security.jwt.JwtUtils;
import ma.messaging.usermanagementservice.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import java.util.stream.Collectors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RedisService redisService;

    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

    @Override
    public ResponseEntity<?> userEdit(@PathVariable("id") int id, EditRequest request, @CookieValue(name = "${application.security.jwt.cookie-name}", required = true) String jwtToken) {
        // get user that is doing the request
        // find account that will be edited
        String usernameFromJwt = jwtUtils.getUserNameFromJwtToken(jwtToken);
        try {
            Account userRequesting = accountRepository.findByUsername(usernameFromJwt)
                                              .orElseThrow(() -> new RuntimeException("Error: User is not found."));
            Account oldAccount = accountRepository.findById(id)
                                              .orElseThrow(() -> new RuntimeException("Error: User is not found."));
                                              
            String usernameFromId = oldAccount.getUsername();

            // if user requesting isnt admin and wants to change someone else's data, do not allow 
            boolean[] isAdmin = { false };
            List<Role> roles = accountRepository.findRolesByAccountId(userRequesting.getAccount_id());
            roles.forEach(role -> {
                System.out.println(role.getName());
                if (role.getName() == ERole.ROLE_ADMIN) {
                    isAdmin[0] = true; 
                } 
            });
            if (!usernameFromJwt.equals(usernameFromId) && isAdmin[0] == false) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to edit this item.");
            }

            String newEmail = request.getEmail();
            String newFirstName = request.getFirstName();
            String newLastName = request.getLastName();
            String newPassword = request.getPassword();

            // if the email is taken, by another user, dont allow it
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
            // update redis according to the database
            redisService.editUserInRedis(oldAccount);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        return ResponseEntity.ok(new MessageResponse("User data updated successfully!"));
    }

    @Override
    public ResponseEntity<?> userDelete(@PathVariable("id") int id, @CookieValue(name = "${application.security.jwt.cookie-name}", required = true) String jwtToken) {
        // get user that is doing the request
        // find account that will be deleted
        String usernameFromJwt = jwtUtils.getUserNameFromJwtToken(jwtToken);
        logger.info(String.format("usernameFromJwt=%s", usernameFromJwt));
        try {
            Account userRequesting = accountRepository.findByUsername(usernameFromJwt)
                    .orElseThrow(() -> new RuntimeException("Error: User is not found."));
            logger.info(String.format("userRequesting=%s", userRequesting));

            Account user = accountRepository.findAccountByAccount_id(id)
                    .orElseThrow(() -> new RuntimeException("Error: User is not found."));
            logger.info(String.format("user=%s", user));

            // if user requesting isnt admin and wants to delete someone else's account, do not allow 
            String usernameFromId = user.getUsername();
            boolean[] isAdmin = {false};
            List<Role> roles = accountRepository.findRolesByAccountId(userRequesting.getAccount_id());
            roles.forEach(role -> {
                System.out.println(role.getName());
                if (role.getName() == ERole.ROLE_ADMIN) {
                    isAdmin[0] = true;
                }
            });
            if (!usernameFromJwt.equals(usernameFromId) && isAdmin[0] == false) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this item.");
            }
            accountRepository.deleteAccountByAccount_id(id);
            // update redis according to the database
            redisService.deleteUserFromRedis(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        return ResponseEntity.ok(new MessageResponse("User deleted successfully!"));
    }

    @Override
    public ResponseEntity<?> getUser(@PathVariable("id") int id, @CookieValue(name = "${application.security.jwt.cookie-name}", required = true) String jwtToken) {
        // get user that is doing the request
        // find account 
        String usernameFromJwt = jwtUtils.getUserNameFromJwtToken(jwtToken);
        try {
            Account userRequesting = accountRepository.findByUsername(usernameFromJwt)
                                              .orElseThrow(() -> new RuntimeException("Error: User is not found."));
            Account user = accountRepository.findById(id)
                                              .orElseThrow(() -> new RuntimeException("Error: User is not found."));
            
            // if user requesting isnt admin and wants to get someone else's account, do not allow 
            String usernameFromId = user.getUsername();
            boolean[] isAdmin = { false };
            List<Role> roles = accountRepository.findRolesByAccountId(userRequesting.getAccount_id());
            roles.forEach(role -> {
                System.out.println(role.getName());
                if (role.getName() == ERole.ROLE_ADMIN) {
                    isAdmin[0] = true; 
                } 
            });

            if (!usernameFromJwt.equals(usernameFromId) && isAdmin[0] == false) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this item.");
            }
            // do not return password
            user.setPassword("");
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @Override
    public ResponseEntity<?> getUsers(@CookieValue(name = "${application.security.jwt.cookie-name}", required = true) String jwtToken, int pageNo, int pageSize) {
        String usernameFromJwt = jwtUtils.getUserNameFromJwtToken(jwtToken);
        try {
            Account userRequesting = accountRepository.findByUsername(usernameFromJwt)
                                              .orElseThrow(() -> new RuntimeException("Error: User is not found."));
            // add pagination
            Pageable pageable = PageRequest.of(pageNo, pageSize);
            Page<Account> accountPage = accountRepository.findAll(pageable);
            
            // get all users except the one requesting
            List<Account> allUsers = accountPage.getContent().stream()
                                                            // .filter(account -> !account.getUsername().equals(userRequesting.getUsername()))
                                                            .collect(Collectors.toList());
            // create pairs of user requesting and every other user, and generate a chat id for every pair
            for (Account user : allUsers) {
                if (!redisService.isUserPairInRedis(String.valueOf(userRequesting.getAccount_id()), String.valueOf(user.getAccount_id()))) {
                    String uniqueId = redisService.generateChatId(String.valueOf(userRequesting.getAccount_id()), String.valueOf(user.getAccount_id()));
                    String redisKey = "chat:" + uniqueId;
                    String jsonValue = "{\"user1\": {" +
                    "\"username\": \"" + userRequesting.getUsername() + "\", " +
                    "\"id\": \"" + userRequesting.getAccount_id() + "\", " +
                    "\"email\": \"" + userRequesting.getEmail() + "\", " +
                    "\"lastName\": \"" + userRequesting.getLastName() + "\", " +
                    "\"firstName\": \"" + userRequesting.getFirstName() + "\"}, " +
                    "\"user2\": {" +
                    "\"username\": \"" + user.getUsername() + "\", " +
                    "\"id\": \"" + user.getAccount_id() + "\", " +
                    "\"email\": \"" + user.getEmail() + "\", " +
                    "\"lastName\": \"" + user.getLastName() + "\", " +
                    "\"firstName\": \"" + user.getFirstName() + "\"}}";
                    redisService.storeKeyValuePair(redisKey, jsonValue);
                }
            }
            Map<String, String> redisData = redisService.getAllKeyValuePairs(userRequesting);
            int totalPages = accountPage.getTotalPages();
            RedisResponse response = new RedisResponse(redisData, totalPages);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No users found");
        }
    }
}

