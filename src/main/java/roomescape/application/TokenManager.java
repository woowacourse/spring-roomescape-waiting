package roomescape.application;

import jakarta.servlet.http.Cookie;

public interface TokenManager {
    String createToken(String payload);

    String getPayload(String token);

    String extractToken(Cookie[] cookies);

    Cookie addTokenToCookie(String accessToken);

    void validateExpiration(String token);
}
