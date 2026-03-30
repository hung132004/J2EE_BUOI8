package hung.demo.config;

import hung.demo.entity.Authority;
import hung.demo.entity.Category;
import hung.demo.entity.User;
import hung.demo.repository.AuthorityRepository;
import hung.demo.repository.CategoryRepository;
import hung.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            // Create authorities
            Authority adminRole = authorityRepository.findByName("ROLE_ADMIN");
            if (adminRole == null) {
                adminRole = new Authority("ROLE_ADMIN");
                authorityRepository.save(adminRole);
            }

            Authority userRole = authorityRepository.findByName("ROLE_USER");
            if (userRole == null) {
                userRole = new Authority("ROLE_USER");
                authorityRepository.save(userRole);
            }

            // Create default users if they don't exist
            User adminUser = userService.findByUsername("admin");
            if (adminUser == null) {
                adminUser = new User("admin", "admin123", "Administrator");
                Set<Authority> adminAuthorities = new HashSet<>();
                adminAuthorities.add(adminRole);
                adminUser.setAuthorities(adminAuthorities);
                userService.save(adminUser);
            }

            User normalUser = userService.findByUsername("user");
            if (normalUser == null) {
                normalUser = new User("user", "user123", "Normal User");
                Set<Authority> userAuthorities = new HashSet<>();
                userAuthorities.add(userRole);
                normalUser.setAuthorities(userAuthorities);
                userService.save(normalUser);
            }

            if (categoryRepository.count() == 0) {
                categoryRepository.save(createCategory("Laptop"));
                categoryRepository.save(createCategory("Dien thoai"));
                categoryRepository.save(createCategory("Phu kien"));
            }
        };
    }

    private Category createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        return category;
    }
}
