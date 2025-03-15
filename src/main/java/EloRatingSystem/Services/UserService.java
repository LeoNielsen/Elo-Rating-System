package EloRatingSystem.Services;

import EloRatingSystem.Dtos.UserDto;
import EloRatingSystem.Dtos.UserRequestDto;
import EloRatingSystem.Dtos.UserResponseDto;
import EloRatingSystem.Models.User;
import EloRatingSystem.Reporitories.UserRepository;
import EloRatingSystem.UserRoles.Role;
import EloRatingSystem.Util.JwtUtil;
import EloRatingSystem.Util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    PasswordUtil passwordUtil;

    public Mono<UserResponseDto> registerUser(UserRequestDto requestDto) {
        if (requestDto.getUsername() == null || requestDto.getPassword() == null) {
            return Mono.error(new IllegalArgumentException("Username and password cannot be null."));
        }

        String encodedPassword = passwordUtil.encodePassword(requestDto.getPassword());
        User newUser = new User();
        newUser.setUsername(requestDto.getUsername());
        newUser.setPassword(encodedPassword);
        newUser.setRole(Role.USER);
        return Mono.just(new UserResponseDto(jwtUtil.generateToken(new UserDto(newUser)), new UserDto(userRepository.save(newUser))));
    }

    public Mono<UserResponseDto> loginUser(UserRequestDto requestDto) {
        if (requestDto.getUsername() == null || requestDto.getPassword() == null) {
            return Mono.error(new IllegalArgumentException("Username and password cannot be null."));
        }

        return Mono.just(userRepository.findByUsername(requestDto.getUsername())
                .filter(user -> passwordUtil.matches(requestDto.getPassword(), user.getPassword()))
                .map(user -> new UserResponseDto(jwtUtil.generateToken(new UserDto(user)), new UserDto(user)))
                .orElseThrow());
    }

}