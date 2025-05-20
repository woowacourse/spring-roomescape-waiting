package roomescape.auth.infrastructure;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class CookieManager {

    public Cookie makeCookie(final String name, final String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    public void deleteCookie(final HttpServletResponse response, final String name) {
        Cookie cookie = makeCookie(name, null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
