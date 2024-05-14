package roomescape.auth.service;

import jakarta.servlet.http.Cookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import roomescape.exception.UnauthorizedException;

import java.util.Arrays;

@Service
public class TokenCookieService {

    public static final String COOKIE_TOKEN_KEY = "token";

    public String createTokenCookie(String value, long maxAge) {
        return ResponseCookie.from(COOKIE_TOKEN_KEY, value)
                .maxAge(maxAge)
                .build()
                .toString();
    }

    public String getTokenFromCookies(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(c -> c.getName().equals(COOKIE_TOKEN_KEY))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("사용자 인증 정보가 없습니다."))
                .getValue();
    }
}
