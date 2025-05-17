package roomescape.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {

    private static final int MAX_AGE = 3600;

    private CookieUtil() {
    }

    public static void addCookie(String name, String value, HttpServletResponse response) {
        addCookie(name, value, MAX_AGE, response);
    }

    public static void addCookie(
            String name,
            String value,
            int maxAge,
            HttpServletResponse response
    ) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public static void expireCookie(String name, HttpServletResponse response) {
        addCookie(name, null, 0, response);
    }
}
