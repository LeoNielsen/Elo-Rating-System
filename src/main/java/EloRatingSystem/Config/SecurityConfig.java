package EloRatingSystem.Config;

import EloRatingSystem.Filter.JwtAuthenticationFilter;
import EloRatingSystem.Filter.UserRoleFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserRoleFilter userRoleFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, UserRoleFilter userRoleFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userRoleFilter = userRoleFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                            logAuthDetails("Before auth check");
                            auth
                                    .requestMatchers("/auth/**").permitAll()
                                    .requestMatchers("/player/**").hasAuthority("USER")

                                    .anyRequest().authenticated();
                            logAuthDetails("After auth check");
                        }
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(userRoleFilter, JwtAuthenticationFilter.class);
        System.out.println("here");
        return http.build();
    }

    private void logAuthDetails(String phase) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            System.out.println(phase + " - Authenticated User: " + auth.getName());
            System.out.println(phase + " - Authorities: " + auth.getAuthorities());
        } else {
            System.out.println(phase + " - No Authentication found in SecurityContext");
        }
    }
}
