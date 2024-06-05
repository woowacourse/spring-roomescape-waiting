package roomescape.util;

import static roomescape.exception.RoomescapeExceptionCode.INVALID_TOKEN;

import java.util.Arrays;
import java.util.Objects;

import jakarta.servlet.http.Cookie;

import roomescape.exception.RoomescapeException;

public class CookieUtil {

    public static final String TOKEN_NAME = "token";

    protected CookieUtil() {}

    public static Cookie create(String value) {
        Cookie cookie = new Cookie(TOKEN_NAME, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }

    public static String extractToken(Cookie[] cookies) {
        if (cookies == null) {
            throw new RoomescapeException(INVALID_TOKEN);
        }
        return Arrays.stream(cookies)
                .filter(cookie -> Objects.equals(TOKEN_NAME, cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new RoomescapeException(INVALID_TOKEN));
    }

    public static Cookie expired() {
        Cookie cookie = new Cookie(TOKEN_NAME, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }
}
