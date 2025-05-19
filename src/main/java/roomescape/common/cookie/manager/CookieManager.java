package roomescape.common.cookie.manager;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface CookieManager {

    void setCookie(HttpServletResponse response, Cookie cookie);

    String extractCookie(HttpServletRequest request, String cookieName);
}
