package roomescape.service.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.exception.AuthenticationException;

@Component
public class TokenProvider {

    private static final String TOKEN = "token";

    private final Key key;
    private final long validityInMilliseconds;

    public TokenProvider(@Value("${security.jwt.token.secret-key}") String secretKey,
                         @Value("${security.jwt.token.expire-length}") long validityInMilliseconds) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.validityInMilliseconds = validityInMilliseconds;
    }

    public String generateAccessToken(long memberId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .signWith(key)
                .expiration(validity)
                .compact();
    }

    public Long parseToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new AuthenticationException("잘못된 토큰 정보입니다.");
        }
        return Long.valueOf(Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody().getSubject());
    }

    public String extractTokenFromCookie(Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> TOKEN.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
