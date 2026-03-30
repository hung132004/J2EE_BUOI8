package hung.demo.controller;

import hung.demo.entity.Authority;
import hung.demo.entity.User;
import hung.demo.repository.AuthorityRepository;
import hung.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashSet;
import java.util.Set;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthorityRepository authorityRepository;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, 
                          @RequestParam String password, 
                          @RequestParam String fullName) {
        User existingUser = userService.findByUsername(username);
        if (existingUser != null) {
            return "redirect:/register?error";
        }

        User user = new User(username, password, fullName);
        
        // Assign USER role by default
        Authority userRole = authorityRepository.findByName("ROLE_USER");
        if (userRole == null) {
            userRole = new Authority("ROLE_USER");
            authorityRepository.save(userRole);
        }
        
        Set<Authority> authorities = new HashSet<>();
        authorities.add(userRole);
        user.setAuthorities(authorities);

        userService.save(user);
        return "redirect:/login?success";
    }
}
