package roomescape.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

public class CookieUtils {

    public static final String TOKEN_KEY = "token";

    private CookieUtils() {
    }

    public static String extractTokenFrom(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(TOKEN_KEY))
                .findAny()
                .map(Cookie::getValue)
                .orElseThrow(() -> new IllegalArgumentException("인증되지 않은 사용자 입니다."));
    }

    public static void setCookieBy(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(TOKEN_KEY, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
