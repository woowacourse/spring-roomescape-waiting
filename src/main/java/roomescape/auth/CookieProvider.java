package roomescape.auth;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;
import roomescape.exception.AuthorizationException;

import java.util.Arrays;

@Component
public class CookieProvider {

    private static final String TOKEN_KEY = "token";

    public Cookie createCookie(final String token) {
        final Cookie cookie = new Cookie(TOKEN_KEY, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    public String extractToken(final Cookie[] cookies) {
        if (cookies == null) {
            throw new AuthorizationException("로그인이 필요합니다.");
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(TOKEN_KEY))
                .findFirst()
                .orElseThrow(() -> new AuthorizationException("로그인이 필요합니다."))
                .getValue();
    }

    public Cookie expireCookie() {
        final Cookie cookie = new Cookie(TOKEN_KEY, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        return cookie;
    }
}
