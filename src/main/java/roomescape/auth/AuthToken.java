package roomescape.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import roomescape.exception.auth.AuthenticationException;

import static org.springframework.boot.web.server.Cookie.SameSite;
import static roomescape.exception.SecurityErrorCode.TOKEN_NOT_EXIST;

public record AuthToken(
        String value
) {
    private static final String TOKEN_NAME = "authToken";

    public static AuthToken extractFrom(final HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(TOKEN_NAME)) {
                    return new AuthToken(cookie.getValue());
                }
            }
        }

        throw new AuthenticationException(TOKEN_NOT_EXIST);
    }

    public HttpHeaders toHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        ResponseCookie cookie = ResponseCookie.from(TOKEN_NAME, value)
                .httpOnly(true)
                .sameSite(SameSite.STRICT.name())
                .build();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
        return headers;
    }
}
