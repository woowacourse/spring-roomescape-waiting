package roomescape.utility;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import roomescape.exception.NotFoundException;

@Component
public class CookieUtility {

    public Cookie getCookie(HttpServletRequest request, String key) {
        validateCookiesInRequest(request);
        for (Cookie cookie : request.getCookies()) {
            if (key.equals(cookie.getName())) {
                return cookie;
            }
        }
        throw new NotFoundException("쿠키를 찾을 수 없습니다.");
    }

    private void validateCookiesInRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new NotFoundException("쿠키가 존재하지 않습니다.");
        }
    }
}
