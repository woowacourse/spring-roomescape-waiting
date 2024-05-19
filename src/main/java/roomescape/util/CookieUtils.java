package roomescape.util;

import jakarta.servlet.http.Cookie;
import roomescape.exception.UnauthorizedException;

import java.util.Arrays;

public class CookieUtils {
    private static final String KEY = "token";

    public static String extractTokenFromCookie(Cookie[] cookies) {
        if (cookies == null) {
            throw new UnauthorizedException();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(KEY))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(UnauthorizedException::new);
    }

    public static Cookie createCookie(String token) {
        Cookie cookie = new Cookie(KEY, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    public static Cookie expireCookie() {
        Cookie cookie = new Cookie(KEY, null);
        cookie.setMaxAge(0);
        return cookie;
    }
}
