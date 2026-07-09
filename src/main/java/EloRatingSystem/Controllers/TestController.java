package EloRatingSystem.Controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
@Slf4j
public class TestController {

    @GetMapping("")
    public String Test() {
        return "Hello! From Backend!";
    }

    @GetMapping("admin")
    @PreAuthorize("hasRole('admin')")
    public String TestAdmin() {
        return "Hello! From Admin!";
    }

    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public String testUser(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaim("preferred_username");
        return "Hello from " + username;
    }




}
