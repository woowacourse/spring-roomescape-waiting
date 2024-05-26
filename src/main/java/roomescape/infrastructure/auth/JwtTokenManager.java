package roomescape.infrastructure.auth;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import javax.crypto.SecretKey;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import roomescape.application.TokenManager;
import roomescape.exception.RoomescapeException;

@Component
public class JwtTokenManager implements TokenManager {
    private static final String TOKEN_KEY = "token";
    private static final int ONE_MINUTE = 60;

    private final JwtTokenProperties jwtTokenProperties;

    public JwtTokenManager(JwtTokenProperties jwtTokenProperties) {
        this.jwtTokenProperties = jwtTokenProperties;
    }

    public String createToken(String payload) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtTokenProperties.getExpireMilliseconds());

        String secretKey = jwtTokenProperties.getSecretKey();
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(String.valueOf(payload))
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    public String getPayload(String token) {
        String secretString = jwtTokenProperties.getSecretKey();
        SecretKey key = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));

        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            throw new RoomescapeException(HttpStatus.UNAUTHORIZED, "인증 유효기간이 만료되었습니다.");
        }
    }

    @Override
    public String extractToken(Cookie[] cookies) {
        if (cookies == null || Arrays.stream(cookies).anyMatch(Objects::isNull)) {
            throw new RoomescapeException(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다.");
        }
        return getAccessToken(cookies);
    }

    private String getAccessToken(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(TOKEN_KEY))
                .findAny()
                .map(Cookie::getValue)
                .orElseThrow(() -> new RoomescapeException(HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다."));
    }

    @Override
    public Cookie addTokenToCookie(String accessToken) {
        Cookie cookie = new Cookie(TOKEN_KEY, accessToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(ONE_MINUTE * 5000000);
        return cookie;
    }

    @Override
    public void validateExpiration(String token) {
        String secretString = jwtTokenProperties.getSecretKey();
        SecretKey key = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));

        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
        } catch (ExpiredJwtException e) {
            throw new RoomescapeException(HttpStatus.UNAUTHORIZED, "인증 유효기간이 만료되었습니다.");
        }
    }
}
