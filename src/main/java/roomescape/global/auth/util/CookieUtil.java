package roomescape.global.auth.util;

import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    public Cookie createCookie(String parameter, String value) {
        Cookie cookie = new Cookie(parameter, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    public Cookie expireCookie(String parameter, String value) {
        Cookie cookie = new Cookie(parameter, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }

    public String extractValueFromCookie(Cookie[] cookies, String name) {
        if (cookies == null) {
            return "";
        }

        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseGet(() -> "");
    }
}
