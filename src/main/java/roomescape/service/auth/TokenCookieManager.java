package roomescape.service.auth;

import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import roomescape.exception.AuthenticationException;

@Component
public class TokenCookieManager {

    private static final String TOKEN_COOKIE_NAME = "token";

    public ResponseCookie createTokenCookie(String token) {
        return getResponseCookieByTokenValue(token);
    }

    public ResponseCookie expireTokenCookie() {
        return getResponseCookieByTokenValue(null);
    }

    private ResponseCookie getResponseCookieByTokenValue(String value) {
        return ResponseCookie
                .from(TOKEN_COOKIE_NAME, value)
                .httpOnly(true)
                .path("/")
                .build();
    }

    public String extractTokenBy(Cookie[] cookies) {
        if (cookies == null || cookies.length == 0) {
            throw new AuthenticationException("쿠키 정보가 존재하지 않습니다.");
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(TOKEN_COOKIE_NAME))
                .findAny()
                .map(Cookie::getValue)
                .orElseThrow(() -> new AuthenticationException("쿠키에 토큰 정보가 존재하지 않습니다."));
    }
}
