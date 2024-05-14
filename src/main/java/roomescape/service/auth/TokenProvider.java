package roomescape.service.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.exception.AuthenticationException;

import java.security.Key;
import java.util.Arrays;

@Component
public class TokenProvider {

    private static final String TOKEN = "token";

    private final Key key;

    public TokenProvider(@Value("${jwt.secret}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateAccessToken(long memberId) {
        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .signWith(key)
                .compact();
    }

    public Long parseToken(String token) {
        if (token == "") {
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
            return "";
        }
        return Arrays.stream(cookies)
                .filter(cookie -> TOKEN.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse("");
    }
}
