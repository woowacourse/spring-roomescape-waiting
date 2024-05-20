package roomescape.util;

import jakarta.servlet.http.Cookie;

import javax.naming.AuthenticationException;
import java.util.Arrays;
import java.util.Objects;

public class TokenExtractor {

    private TokenExtractor() {
    }

    public static String extractTokenFromCookie(Cookie[] cookies) throws AuthenticationException {
        validateNull(cookies);
        return Arrays.stream(cookies)
                .filter(cookie -> Objects.equals(cookie.getName(), "token"))
                .findFirst()
                .orElseThrow(() -> new AuthenticationException("토큰이 없습니다."))
                .getValue();
    }

    private static void validateNull(Cookie[] cookies) throws AuthenticationException {
        if (cookies == null) {
            throw new AuthenticationException("쿠키가 없습니다.");
        }
    }
}
