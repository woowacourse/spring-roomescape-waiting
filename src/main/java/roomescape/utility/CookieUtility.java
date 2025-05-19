package roomescape.utility;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.stereotype.Component;
import roomescape.exception.local.NotFoundCookieException;

@Component
public class CookieUtility {

    public Optional<Cookie> findCookie(HttpServletRequest request, String key) {
        validateCookiesInRequest(request);
        for (Cookie cookie : request.getCookies()) {
            if (key.equals(cookie.getName())) {
                return Optional.of(cookie);
            }
        }
        return Optional.empty();
    }

    private void validateCookiesInRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new NotFoundCookieException();
        }
    }
}
