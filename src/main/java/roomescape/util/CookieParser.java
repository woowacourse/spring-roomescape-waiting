package roomescape.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

public class CookieParser {

    public static Optional<Cookie> findCookie(final HttpServletRequest request, final String name) {
        final Cookie[] cookies = request.getCookies();
        final Cookie cookieByName = findCookieByName(name, cookies);

        if (cookieByName == null) {
            return Optional.empty();
        }

        return Optional.of(cookieByName);
    }

    private static Cookie findCookieByName(final String name, final Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }

        for (final Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie;
            }
        }

        return null;
    }
}
