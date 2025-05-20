package roomescape.login.application;

import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import roomescape.common.exception.impl.UnauthorizedException;

@Service
public class TokenCookieService {

    public static final String COOKIE_TOKEN_KEY = "token";

    public String createTokenCookie(final String value, final long maxAge) {
        return ResponseCookie.from(COOKIE_TOKEN_KEY, value)
                .maxAge(maxAge)
                .build()
                .toString();
    }

    public String getTokenFromCookies(final Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(c -> c.getName().equals(COOKIE_TOKEN_KEY))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("로그인이 필요합니다."))
                .getValue();
    }
}
