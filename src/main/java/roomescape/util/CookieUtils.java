package roomescape.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

public class CookieUtils {
    public static final String TOKEN_KEY = "token";
    public static final String PATH = "/";
    public static final int COOKIE_MAX_AGE = 3600;

    private CookieUtils() {
    }

    public static String extractTokenFromCookie(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(TOKEN_KEY))
                .findAny()
                .map(Cookie::getValue)
                .orElse(null);
    }

    public static void setCookieByToken(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(TOKEN_KEY, token);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setPath(PATH);
        response.addCookie(cookie);
    }

    public static void clearTokenAndCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(TOKEN_KEY, null);
        cookie.setMaxAge(0);
        cookie.setPath(PATH);
        response.addCookie(cookie);
    }
}
