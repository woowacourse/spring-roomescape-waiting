package roomescape.auth.infrastructure.handler.token.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import roomescape.auth.infrastructure.handler.token.TokenAuthorizationHandler;

@Component
public class CookieTokenAuthorizationHandler extends TokenAuthorizationHandler {

    private static final String TOKEN_NAME = "token";

    @Override
    public String getToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(TOKEN_NAME))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
    }

    @Override
    public void setToken(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(TOKEN_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    @Override
    public void removeToken(HttpServletResponse response) {
        Cookie cookie = new Cookie(TOKEN_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
