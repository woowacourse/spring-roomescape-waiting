package roomescape.auth.token;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import roomescape.auth.exception.UnauthorizedException;

public class TokenExtractor {

    public static String extract(final HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        validateCookieIsNonNull(cookies);

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("token"))
                .findAny()
                .orElseThrow(() -> new UnauthorizedException("토큰을 찾을 수 없습니다."))
                .getValue();
    }

    private static void validateCookieIsNonNull(final Cookie[] cookies) {
        if (cookies == null) {
            throw new UnauthorizedException("쿠키가 비었습니다.");
        }
    }
}
