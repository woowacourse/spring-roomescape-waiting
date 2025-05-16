package roomescape.auth.infrastructure.util;

import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import roomescape.auth.infrastructure.jwt.JwtProperties;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CookieManager {

    private static final String LOGIN_TOKEN_NAME = "token";
    private static final String PATH = "/";

    private final JwtProperties jwtProperties;

    public ResponseCookie generateLoginCookie(String token) {
        return ResponseCookie.from(LOGIN_TOKEN_NAME, token)
                .httpOnly(true)
                .path(PATH)
                .maxAge(jwtProperties.getExpireLength())
                .build();
    }

    public ResponseCookie generateLogoutCookie() {
        return ResponseCookie.from(LOGIN_TOKEN_NAME, "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
    }

    public String extractLoginToken(final Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(c -> LOGIN_TOKEN_NAME.equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}
