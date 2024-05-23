package roomescape.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;

import java.util.Arrays;
import java.util.Optional;

public class CookieManager {

    private static final String AUTH_KEY = "token";

    public static ResponseCookie createAuthCookie(String token, long maxAgeSeconds) {
        return ResponseCookie
                .from(AUTH_KEY, token)
                .maxAge(maxAgeSeconds)
                .httpOnly(true)
                .path("/")
                .build();
    }

    public static Optional<Cookie> extractAuthCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(AUTH_KEY))
                .findFirst();
    }
}
