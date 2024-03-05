package ma.messaging.usermanagementservice.repository;

import ma.messaging.usermanagementservice.model.Account;
import ma.messaging.usermanagementservice.model.ERole;
import ma.messaging.usermanagementservice.model.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Testcontainers
public class AccountRepositoryTest {

    @Container
    public static PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    @Autowired
    private AccountRepository accountRepository;
    private Account account;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
    }

    @BeforeEach
    void setUp() {
        account = new Account("username",
                "password",
                "firstname",
                "lastname",
                "username@gmail.com");
        account.setRoles(new HashSet<>() {{
            add(new Role(ERole.ROLE_USER));
        }});

        accountRepository.save(account);
    }

    @Test
    void testExistsByEmail() {
        // Test existsByEmail
        Assertions.assertTrue(accountRepository.existsByEmail(account.getEmail()));
        Assertions.assertFalse(accountRepository.existsByEmail("notfound@example.com"));
    }

    @Test
    void testExistsByUsername() {
        // Test existsByUsername
        Assertions.assertTrue(accountRepository.existsByUsername(account.getUsername()));
        Assertions.assertFalse(accountRepository.existsByUsername("non-user"));
    }

    @Test
    void testFindByUsername() {
        // Test findByUsername
        Optional<Account> foundAccount = accountRepository.findByUsername(account.getUsername());
        Assertions.assertTrue(foundAccount.isPresent(), "Account should be found with username testUser");
        assertEquals(account.getUsername(), foundAccount.get().getUsername(), "The username should match");

        // Test findByUsername for a username that does not exist
        Optional<Account> notFoundAccount = accountRepository.findByUsername("nonExistingUser");
        assertFalse("Account should not be found", notFoundAccount.isPresent());
    }

    @Test
    void testFindById() {
        // Test findById
        Optional<Account> foundAccount = accountRepository.findById(1);
        Assertions.assertTrue(foundAccount.isPresent(), "Account should be found with id = 1");

        // Test findById for an id that does not exist
        Optional<Account> notFoundAccount = accountRepository.findById(2);
        assertFalse("Account should not be found", notFoundAccount.isPresent());
    }

    @Test
    void testDeleteById() {
        // Test deleteById
        accountRepository.deleteById(1);
        Optional<Account> foundAccount = accountRepository.findById(1);
        Assertions.assertFalse(foundAccount.isPresent(), "Account should not be found with id = 1");
    }
}
