package roomescape.auth.presentation;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Optional;

public class TokenExtractor {
    private static final String TOKEN_COOKIE_KEY = "token";

    public static Optional<String> extract(HttpServletRequest servletRequest) {
        Cookie[] cookies = servletRequest.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(TOKEN_COOKIE_KEY))
                .map(Cookie::getValue)
                .findAny();
    }
}
