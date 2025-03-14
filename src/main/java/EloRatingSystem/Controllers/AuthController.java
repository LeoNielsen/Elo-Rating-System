package EloRatingSystem.Controllers;

import EloRatingSystem.Dtos.UserDto;
import EloRatingSystem.Dtos.UserRequestDto;
import EloRatingSystem.Dtos.UserResponseDto;
import EloRatingSystem.Models.User;
import EloRatingSystem.Services.UserService;
import EloRatingSystem.UserRoles.Role;
import JWT.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    public UserService userService;

    @Autowired
    public JwtUtil jwtUtil;

    @Autowired
    public AuthenticationManager authenticationManager;

    // Register a user
    @PostMapping("/register")
    public Mono<ResponseEntity<UserResponseDto>> register(@RequestBody User user) {
        return userService.registerUser(user, Role.ROLE_USER)
                .map(registeredUser -> {
                    // Generate JWT token after registration
                    String token = jwtUtil.createJWT(registeredUser);
                    UserDto userDto = new UserDto(registeredUser.getUsername(), registeredUser.getRole());
                    UserResponseDto responseDto = new UserResponseDto(token, userDto);
                    return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
                });
    }

    // Login a user
    @PostMapping("/login")
    public Mono<ResponseEntity<UserResponseDto>> login(@RequestBody UserRequestDto request) {
        return Mono.fromCallable(() -> authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())))
                .doOnNext(authentication -> SecurityContextHolder.getContext().setAuthentication(authentication))
                .flatMap(authentication -> Mono.fromCallable(() -> userService.findByUsername(request.getUsername())))
                .map(userOptional -> userOptional.map(user -> {
                    String token = jwtUtil.createJWT(new UserDto(user));
                    UserDto userDto = new UserDto(user.getUsername(), user.getRole());
                    UserResponseDto responseDto = new UserResponseDto(token, userDto);
                    return ResponseEntity.ok(responseDto);
                }).orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)));
    }
}
