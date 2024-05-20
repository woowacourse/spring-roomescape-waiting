package roomescape.application;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public interface TokenManager {
    String createToken(String payload);

    String getPayload(String token);

    String extractToken(Cookie[] cookies);

    void setToken(HttpServletResponse response, String accessToken);
}
