package roomescape.infrastructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.security.TokenProvider;

@Component
public class JwtTokenProvider implements TokenProvider {

    private final SecretKey secretKey;
    private final long expirationTime;

    public JwtTokenProvider(
            @Value("${security.jwt.secret-key}") String secretKey,
            @Value("${security.jwt.expiration-time}") long expirationTime
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.expirationTime = expirationTime;
    }

    @Override
    public String createToken(String memberId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .subject(memberId)
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey, SIG.HS256)
                .compact();
    }

    @Override
    public Long getMemberId(String token) {
        Claims claims = toClaims(token);

        String memberId = claims.getSubject();
        return Long.parseLong(memberId);
    }

    private Claims toClaims(String token) {
        try {
            Jws<Claims> claimsJws = getClaimsJws(token);

            return claimsJws.getPayload();
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("만료된 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            throw new IllegalArgumentException("지원하지 않는 토큰입니다.");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
    }

    private Jws<Claims> getClaimsJws(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    }
}
