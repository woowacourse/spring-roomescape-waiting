package roomescape.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.web.context.request.NativeWebRequest;

public class CookieUtil {

    private static final String TOKEN = "token";
    private static final int COOKIE_MAX_AGE_ONE_HOUR = 60 * 60;

    private CookieUtil() {
    }

    public static void setTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(TOKEN, token);
        cookie.setMaxAge(COOKIE_MAX_AGE_ONE_HOUR);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        response.addCookie(cookie);
    }

    public static void clearTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(TOKEN, null);
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        response.addCookie(cookie);
    }

    public static Optional<String> extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> TOKEN.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    public static Optional<String> extractTokenFromCookie(NativeWebRequest webRequest) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        if (request == null) {
            return Optional.empty();
        }

        return extractTokenFromCookie(request);
    }
}
