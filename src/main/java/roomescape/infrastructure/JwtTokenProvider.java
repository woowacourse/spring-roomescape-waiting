package roomescape.infrastructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.security.exception.UnauthorizedException;
import roomescape.security.provider.TokenProvider;

@Component
public class JwtTokenProvider implements TokenProvider {

    private final SecretKey secretKey;
    private final long expirationMilliseconds;

    public JwtTokenProvider(@Value("${security.jwt.secret-key}") String secretKey,
                            @Value("${security.jwt.expiration-time}") long expirationMilliseconds) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.expirationMilliseconds = expirationMilliseconds;
    }

    @Override
    public String createToken(String subject) {
        Date validity = new Date(System.currentTimeMillis() + expirationMilliseconds);
        return Jwts.builder()
                .subject(subject)
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String extractSubject(String token) {
        Claims claims = toClaims(token);
        return claims.getSubject();
    }

    private Claims toClaims(String token) {
        try {
            Jws<Claims> claimsJws = getClaimsJws(token);
            return claimsJws.getPayload();
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException("만료된 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            throw new UnauthorizedException("지원하지 않는 토큰입니다.");
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("유효하지 않은 토큰입니다.");
        }
    }

    private Jws<Claims> getClaimsJws(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    }
}
