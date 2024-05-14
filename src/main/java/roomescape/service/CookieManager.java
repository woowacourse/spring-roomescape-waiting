package roomescape.service;

import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import roomescape.exception.BadRequestException;

@Component
public class CookieManager {

    private static final String TOKEN_COOKIE_NAME = "token";

    public ResponseCookie create(String token) {
        return ResponseCookie
                .from(TOKEN_COOKIE_NAME, token)
                .httpOnly(true)
                .path("/")
                .build();
    }

    public ResponseCookie delete() {
        return ResponseCookie
                .from(TOKEN_COOKIE_NAME, null)
                .httpOnly(true)
                .path("/")
                .build();
    }

    public String extractToken(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(TOKEN_COOKIE_NAME))
                .findAny()
                .map(Cookie::getValue)
                .orElseThrow(() -> new BadRequestException("올바르지 않은 토큰값입니다."));
    }
}
