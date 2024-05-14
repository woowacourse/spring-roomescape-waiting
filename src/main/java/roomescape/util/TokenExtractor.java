package roomescape.util;

import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Objects;
import javax.naming.AuthenticationException;

public class TokenExtractor {

    private TokenExtractor() {
    }

    public static String extractTokenFromCookie(Cookie[] cookies) throws AuthenticationException {
        validateNull(cookies);
        return Arrays.stream(cookies)
                .filter(cookie -> Objects.equals(cookie.getName(), "token"))
                .findFirst()
                .orElseThrow(() -> new AuthenticationException("접근 권한 확인을 위한 쿠키가 없습니다."))
                .getValue();
    }

    private static void validateNull(Cookie[] cookies) throws AuthenticationException {
        if (cookies == null) {
            throw new AuthenticationException("접근 권한 확인을 위한 쿠키가 없습니다.");
        }
    }
}
