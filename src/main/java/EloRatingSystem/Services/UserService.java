package EloRatingSystem.Services;

import EloRatingSystem.Models.User;
import EloRatingSystem.Reporitories.UserRepository;
import EloRatingSystem.UserRoles.Role;
import JWT.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {

    @Autowired
    public UserRepository userRepository;
    @Autowired
    public JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User registerUser(User user, Role role) {
        // Hash the password before saving
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        user.setRole(role);

        // Save the user
        User savedUser = userRepository.save(user);

        // Generate JWT after user is saved
        String jwt = jwtUtil.createJWT(savedUser);
        System.out.println("Generated JWT for user " + savedUser.getUsername() + ": " + jwt);

        return savedUser;
    }

    public void createAdminIfNotExists(){
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));  // Hash the password
            admin.setRole(Role.ROLE_ADMIN);
            userRepository.save(admin);

            // Optionally generate JWT for admin
            String adminJwt = jwtUtil.createJWT(admin);
            System.out.println("Generated JWT for admin: " + adminJwt);
        }
    }
}
