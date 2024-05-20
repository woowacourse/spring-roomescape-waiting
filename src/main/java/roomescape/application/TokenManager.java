package roomescape.application;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;

public interface TokenManager {
    String createToken(String payload);

    String getPayload(String token);

    String extractToken(Cookie[] cookies);

    void setToken(HttpServletResponse response, String accessToken);

    Date getExpiration(String token);
}
