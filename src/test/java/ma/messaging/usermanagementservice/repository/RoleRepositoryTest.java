package ma.messaging.usermanagementservice.repository;

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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Testcontainers
public class RoleRepositoryTest {

    @Container
    public static PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    @Autowired
    private RoleRepository roleRepository;
    private Role roleUser;
    private Role roleAdmin;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
    }

    @BeforeEach
    void setUp() {
        roleUser = new Role(ERole.ROLE_USER);
        roleAdmin = new Role(ERole.ROLE_ADMIN);

        roleRepository.save(roleUser);
        roleRepository.save(roleAdmin);
    }

    @Test
    void findByNameRoleUser() {
        Optional<Role> foundRole = roleRepository.findByName(ERole.ROLE_USER);

        Assertions.assertTrue(foundRole.isPresent());
        assertEquals(foundRole.get().getName(), ERole.ROLE_USER);
    }

    @Test
    void findByNameRoleAdmin() {
        Optional<Role> foundRole = roleRepository.findByName(ERole.ROLE_ADMIN);

        Assertions.assertTrue(foundRole.isPresent());
        assertEquals(foundRole.get().getName(), ERole.ROLE_ADMIN);
    }

}
