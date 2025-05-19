package roomescape.utility;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import roomescape.exception.local.NotFoundCookieException;

@Component
public class CookieUtility {

    public Cookie getCookie(HttpServletRequest request, String key) {
        validateCookiesInRequest(request);
        for (Cookie cookie : request.getCookies()) {
            if (key.equals(cookie.getName())) {
                return cookie;
            }
        }
        throw new NotFoundCookieException();
    }

    private void validateCookiesInRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new NotFoundCookieException();
        }
    }
}
