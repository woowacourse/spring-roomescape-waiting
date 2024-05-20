package roomescape.util;

import jakarta.servlet.http.Cookie;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class CookieUtil {

    private static final String TOKEN_NAME = "token";

    public static Optional<String> extractToken(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> Objects.equals(TOKEN_NAME, cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    public static String makeTokenCookie(String token) {
        return TOKEN_NAME + "=" + token;
    }

    public static Cookie makeCookieExpired() {
        Cookie cookie = new Cookie(TOKEN_NAME, null);
        cookie.setMaxAge(0);
        return cookie;
    }
}
