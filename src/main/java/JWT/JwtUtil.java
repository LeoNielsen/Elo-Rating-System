package JWT;

import EloRatingSystem.Dtos.UserDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.InitializingBean;

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
        System.out.println("Secret key rotated: " + new Date());
    }

    public String createJWT(UserDto user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                .signWith(secretKey.get(), SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey.get()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractSubject(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey.get()).build().parseClaimsJws(token).getBody().getSubject();
    }
}