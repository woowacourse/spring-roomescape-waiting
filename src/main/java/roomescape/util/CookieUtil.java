package roomescape.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import jakarta.servlet.http.Cookie;

import roomescape.controller.exception.BaseException;

public class CookieUtil {

    private static final String TOKEN_NAME = "token";

    protected CookieUtil() {}

    public static Optional<String> extractToken(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> Objects.equals(TOKEN_NAME, cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    public static Cookie expired() {
        Cookie cookie = new Cookie(TOKEN_NAME, null);
        cookie.setMaxAge(0);
        return cookie;
    }

    public static <X extends BaseException> Cookie[] requireNonnull(Cookie[] cookies, Supplier<X> exception) {
        if (cookies != null) {
            return cookies;
        }
        throw exception.get();
    }
}
