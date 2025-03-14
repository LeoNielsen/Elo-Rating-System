package EloRatingSystem.Controllers;

import EloRatingSystem.Models.User;
import EloRatingSystem.Reporitories.UserRepository;
import EloRatingSystem.Services.UserService;
import EloRatingSystem.UserRoles.Role;
import JWT.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    // Register endpoint
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) throws Exception {
        // Register the user with 'ROLE_USER'
        User savedUser = userService.registerUser(user, Role.ROLE_USER);
        return ResponseEntity.ok(savedUser);
    }

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        // Authenticate user using Spring Security's AuthenticationManager
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.get("username"), request.get("password"))
            );

            // Set the authentication in the security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Find the user from the repository
            User user = userRepository.findByUsername(request.get("username"))
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Generate the JWT token
            String token = jwtUtil.createJWT(user);

            // Return the token and role
            return ResponseEntity.ok(Map.of("token", token, "role", user.getRole()));

        } catch (Exception e) {
            // Handle authentication error (incorrect username/password)
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }
}
