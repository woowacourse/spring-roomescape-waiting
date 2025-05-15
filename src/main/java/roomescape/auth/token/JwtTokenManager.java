package roomescape.auth.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.RequiredTypeException;
import java.util.Date;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.auth.token.exception.JwtExtractException;

@Component
public class JwtTokenManager {

    private final int expirationTime;
    private final String secretKey;

    public JwtTokenManager(
            @Value("${expiration.time}") final int EXPIRATION_TIME,
            @Value("${secret.key}") final String SECRET_KEY) {
        this.expirationTime = EXPIRATION_TIME;
        this.secretKey = SECRET_KEY;
    }

    public String createToken(final Long id, final String role) {
        SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");

        return Jwts.builder()
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .subject(id.toString())
                .claim("role", role)
                .issuedAt(new Date())
                .signWith(key, SIG.HS256)
                .compact();
    }

    public Claims extractClaims(final String token) {
        try {
            return Jwts.parser()
                    .verifyWith(new SecretKeySpec(secretKey.getBytes(), "HmacSHA256"))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (MalformedJwtException malformedJwtException) {
            throw new JwtExtractException("유효한 토큰이 아닙니다.");
        } catch (ExpiredJwtException expiredJwtException) {
            throw new JwtExtractException("토큰이 만료되었습니다.");
        } catch (IllegalArgumentException argumentException) {
            throw new JwtExtractException("토큰이 비었습니다.");
        } catch (JwtException jwtException) {
            throw new JwtExtractException(jwtException.getMessage());
        }
    }

    public Long getId(final String token) {
        Claims claims = extractClaims(token);
        return parseLongId(claims.getSubject());
    }

    private Long parseLongId(final String rawId) {
        try {
            return Long.valueOf(rawId);
        } catch (NumberFormatException e) {
            throw new JwtExtractException("토큰 정보를 파싱할 수 없습니다.");
        }
    }

    public String getRole(final String token) {
        Claims claims = extractClaims(token);

        try {
            return claims.get("role", String.class);
        } catch (RequiredTypeException requiredTypeException) {
            throw new JwtExtractException("role에 대한 claim을 찾을 수 없습니다.");
        }
    }
}
