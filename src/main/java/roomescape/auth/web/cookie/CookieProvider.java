package roomescape.auth.web.cookie;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;
import roomescape.auth.web.exception.TokenNotFoundException;

@Component
public class CookieProvider {

    private static final String TOKEN_COOKIE_NAME = "token";
    private static final int ONE_DAY_SECONDS = 60 * 60 * 24;

    public String extractTokenFromCookie(Cookie[] cookies) {

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(TOKEN_COOKIE_NAME)) {
                return cookie.getValue();
            }
        }
        throw new TokenNotFoundException("해당 토큰 키가 존재하지 않습니다!");
    }

    public Cookie createTokenCookie(String accessToken) {
        Cookie cookie = new Cookie(TOKEN_COOKIE_NAME, accessToken);
        cookie.setHttpOnly(true);
        cookie.setAttribute("SameSite", "Strict");
        cookie.setPath("/");
        cookie.setMaxAge(ONE_DAY_SECONDS);
        return cookie;
    }

    public Cookie createExpiredTokenCookie() {
        Cookie expiredCookie = new Cookie(TOKEN_COOKIE_NAME, null);
        expiredCookie.setPath("/");
        expiredCookie.setAttribute("SameSite", "Strict");
        expiredCookie.setMaxAge(0);
        expiredCookie.setHttpOnly(true);
        return expiredCookie;
    }
}
