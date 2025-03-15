package EloRatingSystem.Filter;

import EloRatingSystem.UserRoles.Role;
import EloRatingSystem.Util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    public JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("JwtAuthenticationFilter is running for " + request.getRequestURI());
        String token = extractToken(request);
        System.out.println("Extracted Token: " + token);

        if (token != null) {
            String username = jwtUtil.extractUsername(token);
            System.out.println("Extracted Username: " + username);

            String role = jwtUtil.extractRole(token);  // Extract the single role from the token
            System.out.println("Role from Token: " + role);

            if (username != null && jwtUtil.validateToken(token, username)) {
                // Convert the role to a GrantedAuthority
                List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
                // Ensure role is correctly set

                // Create the authentication object with the username and authority
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);

                // Set the details for the authentication object
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set the authentication in the SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);

                System.out.println("Authentication successful for user: " + username);
                System.out.println("Authorities: " + authentication.getAuthorities());

                System.out.println("Expected Authority: " + Role.USER.name());
                System.out.println("Actual Authorities: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());

            } else {
                System.out.println("Token validation failed.");
            }
        } else {
            System.out.println("No token found in request.");
        }

        // Continue with the next filter in the chain
        filterChain.doFilter(request, response);

// Check if authentication is still set after the filter chain
        System.out.println("After filter chain - Auth: " + SecurityContextHolder.getContext().getAuthentication());

    }


    // Extract the token from the Authorization header
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        System.out.println(authHeader);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);  // Skip "Bearer " (7 characters)
        }
        return null;
    }
}
