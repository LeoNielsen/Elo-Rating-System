package EloRatingSystem.Controllers;

import EloRatingSystem.Dtos.UserRequestDto;
import EloRatingSystem.Dtos.UserResponseDto;
import EloRatingSystem.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("/register")
    public Mono<UserResponseDto> register(@RequestBody UserRequestDto request) {
        return userService.registerUser(request);

    }

    @PostMapping("/login")
    public Mono<UserResponseDto> login(@RequestBody UserRequestDto request) {
        return userService.loginUser(request);
    }

}
