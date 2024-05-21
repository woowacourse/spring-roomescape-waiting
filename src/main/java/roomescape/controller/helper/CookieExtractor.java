package roomescape.controller.helper;

import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import roomescape.exception.login.InvalidTokenException;

@Component
public class CookieExtractor {
    private final String COOKIE_NAME = "token";

    public Cookie createCookie(String token) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    public Cookie deleteCookie() {
        Cookie cookie = new Cookie(COOKIE_NAME, null);
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    public String getToken(Cookie[] cookies) {
        if (cookies == null) {
            throw new InvalidTokenException();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(COOKIE_NAME))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(InvalidTokenException::new);
    }
}
