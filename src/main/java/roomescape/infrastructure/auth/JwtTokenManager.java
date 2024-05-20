package roomescape.infrastructure.auth;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;
import roomescape.application.TokenManager;
import roomescape.exception.RoomescapeErrorCode;
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
            throw new RoomescapeException(RoomescapeErrorCode.TOKEN_EXPIRED);
        }
    }

    @Override
    public String extractToken(Cookie[] cookies) {
        if (cookies == null || Arrays.stream(cookies).anyMatch(Objects::isNull)) {
            throw new RoomescapeException(RoomescapeErrorCode.UNAUTHORIZED);
        }
        return getAccessToken(cookies);
    }

    private String getAccessToken(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(TOKEN_KEY))
                .findAny()
                .map(Cookie::getValue)
                .orElseThrow(() -> new RoomescapeException(RoomescapeErrorCode.UNAUTHORIZED, "토큰이 존재하지 않습니다."));
    }

    @Override
    public void setToken(HttpServletResponse response, String accessToken) {
        Cookie cookie = new Cookie(TOKEN_KEY, accessToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(ONE_MINUTE * 5000000);
        response.addCookie(cookie);
    }

    @Override
    public Date getExpiration(String token) {
        String secretString = jwtTokenProperties.getSecretKey();
        SecretKey key = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getBody()
                .getExpiration();
    }
}
