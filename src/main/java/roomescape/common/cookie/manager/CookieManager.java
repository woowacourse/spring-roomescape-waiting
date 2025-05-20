package roomescape.common.cookie.manager;

import jakarta.servlet.http.HttpServletRequest;

public interface CookieManager {

    String extractCookie(HttpServletRequest request, String cookieName);
}
