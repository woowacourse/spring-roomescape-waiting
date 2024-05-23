package roomescape.controller.utils;

import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import roomescape.exception.AuthorizationException;

@Component
public class CookieUtils {

    private static final String AUTH_COOKIE_KEY = "token";

    public static ResponseCookie createCookie(String token, long maxAgeSeconds) {
        return ResponseCookie
                .from(AUTH_COOKIE_KEY, token)
                .maxAge(maxAgeSeconds)
                .httpOnly(true)
                .path("/")
                .build();
    }

    public static String extractToken(Cookie[] cookies) {
        if (cookies == null) {
            throw new AuthorizationException();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(AUTH_COOKIE_KEY))
                .findFirst()
                .orElseThrow(AuthorizationException::new)
                .getValue();
    }
}
