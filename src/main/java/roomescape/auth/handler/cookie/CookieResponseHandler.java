package roomescape.auth.handler.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import roomescape.auth.handler.ResponseHandler;

@Component
public class CookieResponseHandler implements ResponseHandler {

    private static final String SESSION_KEY = "token";

    @Override
    public void set(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(SESSION_KEY, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    @Override
    public void expire(HttpServletResponse response) {
        Cookie cookie = new Cookie(SESSION_KEY, null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
