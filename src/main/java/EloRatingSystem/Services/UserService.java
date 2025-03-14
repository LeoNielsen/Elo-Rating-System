package EloRatingSystem.Services;

import EloRatingSystem.Models.User;
import EloRatingSystem.Dtos.UserDto;
import EloRatingSystem.Reporitories.UserRepository;
import EloRatingSystem.UserRoles.Role;
import JWT.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.Optional;

@Service
@Slf4j
public class UserService {
    @Autowired
    public UserRepository userRepository;

    @Autowired
    public JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Mono<UserDto> registerUser(User user, Role role) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(role);

        return Mono.fromCallable(() -> userRepository.save(user))
                .doOnSuccess(savedUser -> log.info("Generated JWT for user {}: {}", savedUser.getUsername(), jwtUtil.createJWT(new UserDto(savedUser))))
                .map(savedUser -> new UserDto(savedUser.getUsername(), savedUser.getRole()));
    }

    public Mono<Void> createAdminIfNotExists() {
        return Mono.fromCallable(() -> userRepository.findByUsername("admin"))
                .filter(Optional::isEmpty)
                .flatMap(empty -> Mono.fromRunnable(() -> {
                    User admin = new User();
                    admin.setUsername("admin");
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    admin.setRole(Role.ROLE_ADMIN);
                    userRepository.save(admin);
                    log.info("Generated JWT for admin: {}", jwtUtil.createJWT(new UserDto(admin)));
                }))
                .then();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}