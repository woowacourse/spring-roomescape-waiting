package roomescape.common;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import roomescape.common.exception.AuthenticationException;

@Component
public class AuthTokenCookieProvider {

    private static final String TOKEN_NAME = "token";

    @Value("${security.jwt.token.expire-length}")
    private long EXPIRE_MILLI;

    public String extractToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new AuthenticationException("로그인이 필요한 서비스입니다.");
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(TOKEN_NAME)) {
                return cookie.getValue();
            }
        }
        throw new AuthenticationException("로그인이 필요한 서비스입니다.");
    }

    public ResponseCookie generate(String token) {
        return ResponseCookie.from(TOKEN_NAME, token)
                .httpOnly(true)
                .maxAge((int) EXPIRE_MILLI / 1000)
                .path("/")
                .build();
    }

    public ResponseCookie generateExpired() {
        return ResponseCookie.from(TOKEN_NAME)
                .maxAge(0)
                .build();
    }
}

