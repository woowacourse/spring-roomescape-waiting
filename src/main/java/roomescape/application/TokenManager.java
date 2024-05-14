package roomescape.application;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public interface TokenManager {

    String extractToken(Cookie[] cookies);

    void setToken(HttpServletResponse response, String accessToken);
}
