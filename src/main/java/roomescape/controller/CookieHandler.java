package roomescape.controller;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

@Component
public class CookieHandler {

    public Cookie createCookie(String cookieName, String cookieValue) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }
}
