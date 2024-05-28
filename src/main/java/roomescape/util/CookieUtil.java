package roomescape.util;

import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Optional;

public class CookieUtil {
    private CookieUtil() {
    }

    public static Optional<String> searchValueFromKey(Cookie[] cookies, String key) {
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(key))
                .findFirst()
                .map(Cookie::getValue);
    }

    public static Cookie createCookie(String key, String token) {
        Cookie cookie = new Cookie(key, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    public static Cookie expiredCookie(String key) {
        Cookie cookie = new Cookie(key, "");
        cookie.setMaxAge(0);
        return cookie;
    }
}
