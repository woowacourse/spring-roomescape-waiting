package roomescape.infrastructure.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import roomescape.application.exception.AuthException;

import java.util.Arrays;

public class JwtTokenExtractor {

    private JwtTokenExtractor() {}

    public static String extract(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new AuthException("[ERROR] 쿠키가 존재하지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        return Arrays.stream(cookies)
                .filter(cookie -> "token".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new AuthException("[ERROR] 토큰이 존재하지 않습니다.", HttpStatus.UNAUTHORIZED));
    }
}
