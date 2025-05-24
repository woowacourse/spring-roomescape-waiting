package roomescape.member.security;

import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieExtractor {

    private static final String COOKIE_NAME = "token";

    public ResponseCookie createCookie(final String token, final int maxAge) {
        if (token == null) {
            throw new IllegalArgumentException("토큰 값이 null일 수 없습니다");
        }

        return ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    public static String extractToken(final Cookie[] cookies) {
        if (cookies == null || cookies.length == 0) {
            throw new IllegalArgumentException("쿠키가 존재하지 않습니다.");
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(COOKIE_NAME))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new IllegalArgumentException("쿠키 토큰을 추출할 수 없습니다."));
    }
}
