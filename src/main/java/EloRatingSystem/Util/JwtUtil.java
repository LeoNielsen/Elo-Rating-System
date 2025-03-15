package EloRatingSystem.Util;

import EloRatingSystem.Dtos.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class JwtUtil implements InitializingBean {
    private final AtomicReference<Key> secretKey = new AtomicReference<>();

    @Override
    public void afterPropertiesSet() {
        rotateSecretKey();
    }

    @Scheduled(cron = "0 0 0 */90 * ?") // Rotate every 90 days
    public void rotateSecretKey() {
        secretKey.set(Keys.secretKeyFor(SignatureAlgorithm.HS512));
    }

    public String generateToken(UserDto userDto) {
        return Jwts.builder()
                .setSubject(userDto.getUsername())
                .claim("role", userDto.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(secretKey.get(), SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateToken(String token, String username) {
        String tokenUsername = extractUsername(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }

    // Extract username from token
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey.get())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractRole(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey.get())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("role", String.class);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey.get())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }
}