package ma.messaging.usermanagementservice.config;

import ma.messaging.usermanagementservice.model.ERole;
import ma.messaging.usermanagementservice.model.Role;
import ma.messaging.usermanagementservice.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        // Ensure all roles are present in the database
        for (ERole role : ERole.values()) {
            roleRepository.findByName(role).orElseGet(() -> {
                Role newRole = new Role(role);
                roleRepository.save(newRole);
                return newRole;
            });
        }
    }
}