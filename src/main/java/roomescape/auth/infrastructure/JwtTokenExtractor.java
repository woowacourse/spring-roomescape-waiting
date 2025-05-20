package roomescape.auth.infrastructure;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import roomescape.auth.domain.AuthTokenExtractor;
import roomescape.exception.auth.AuthTokenNotFoundException;

@Component
public class JwtTokenExtractor implements AuthTokenExtractor<String> {

    public String extract(final HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new AuthTokenNotFoundException("쿠키가 존재하지 않습니다.");
        }

        return Arrays.stream(cookies)
                .filter(cookie -> AUTH_TOKEN_NAME.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new AuthTokenNotFoundException("쿠키에 " + AUTH_TOKEN_NAME + "이 존재하지 않습니다."));
    }
}
