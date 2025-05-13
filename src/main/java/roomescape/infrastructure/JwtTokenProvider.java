package roomescape.infrastructure;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.auth.Role;
import roomescape.exception.ExpiredTokenException;
import roomescape.exception.InvalidTokenException;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final Long tokenValidTime;

    public JwtTokenProvider(
            @Value("${auth.jwt.secret-key}") String secretKey,
            @Value("${auth.jwt.valid-time}") Long tokenValidTime
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.tokenValidTime = tokenValidTime;
    }

    public String createToken(String payload, Role role) {
        return Jwts.builder()
                .subject(payload)
                .claim("role", role)
                .expiration(new Date(System.currentTimeMillis() + tokenValidTime))
                .signWith(secretKey)
                .compact();
    }

    public String extractSubject(String token) {
        validateToken(token);
        return createTokenParser()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public Role extractRole(String token) {
        validateToken(token);
        return Role.of(
                createTokenParser()
                        .parseSignedClaims(token)
                        .getPayload()
                        .get("role", String.class)
        );
    }

    private void validateToken(String token) {
        try {
            createTokenParser().parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new ExpiredTokenException();
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException();
        }
    }

    private JwtParser createTokenParser() {
        return Jwts.parser().verifyWith(secretKey).build();
    }


}
