package roomescape.service;

import jakarta.servlet.http.Cookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import roomescape.dto.response.TokenResponse;
import roomescape.infrastructure.TokenGenerator;

import java.util.Arrays;

@Service
public class CookieService {

    private final TokenGenerator tokenGenerator;

    public CookieService(final TokenGenerator tokenGenerator) {
        this.tokenGenerator = tokenGenerator;
    }

    public ResponseCookie createCookie(final TokenResponse tokenResponse) {
        final Cookie cookie = new Cookie(tokenGenerator.getCookieName(), tokenResponse.accessToken());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return convertCookieToResponseCookie(cookie);
    }

    public ResponseCookie createEmptyCookie() {
        final Cookie cookie = new Cookie(tokenGenerator.getCookieName(), null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return convertCookieToResponseCookie(cookie);
    }

    private ResponseCookie convertCookieToResponseCookie(final Cookie cookie) {
        return ResponseCookie.from(cookie.getName(), cookie.getValue())
                .path(cookie.getPath())
                .domain(cookie.getDomain())
                .maxAge(cookie.getMaxAge())
                .httpOnly(cookie.isHttpOnly())
                .secure(cookie.getSecure())
                .build();
    }
}
