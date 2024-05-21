package roomescape.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import roomescape.exception.AccessNotAllowException;

import java.util.Arrays;

public class CookieAuthorizationExtractor implements AuthorizationExtractor<String> {
    @Override
    public String extract(final HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new AccessNotAllowException("로그인 정보가 존재하지 않습니다.");
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(TOKEN_NAME))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new AccessNotAllowException("로그인 정보가 존재하지 않습니다."));
    }
}
