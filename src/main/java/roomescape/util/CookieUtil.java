package roomescape.util;

import java.util.Objects;
import java.util.Optional;

import jakarta.servlet.http.Cookie;

public class CookieUtil {

    // TODO: private -> public
    private static final String TOKEN_NAME = "token";

    public static Optional<String> extractToken(Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            if (Objects.equals(TOKEN_NAME, cookie.getName())) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }

    public static String makeTokenCookie(String token) {
        return TOKEN_NAME + "=" + token;
    }

    public static Cookie makeCookieExpired() {
        Cookie cookie = new Cookie(TOKEN_NAME, null);
        cookie.setMaxAge(0);
        return cookie;
    }
}
