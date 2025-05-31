package roomescape.controller.util;

import jakarta.servlet.http.Cookie;
import java.util.Objects;
import org.springframework.stereotype.Component;
import roomescape.exception.UnauthorizationException;

@Component
public class CookieHandler {

    public Cookie createCookie(String cookieName, String cookieValue) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }

    public String extractCookie(Cookie[] cookies, String cookieName) {
        if (cookies == null) {
            throw new UnauthorizationException("쿠키가 존재하지 않습니다.");
        }
        for (Cookie cookie : cookies) {
            if (Objects.equals(cookie.getName(), cookieName)) {
                return cookie.getValue();
            }
        }
        throw new UnauthorizationException("접근 권한이 없습니다.");
    }
}
