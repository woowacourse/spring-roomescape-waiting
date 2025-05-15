package roomescape.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class CookieTokenExtractor {

    private static final String TOKEN = "token";

    public String extract(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) return "";

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(TOKEN)) {
                return cookie.getValue();
            }
        }

        return "";
    }
}
