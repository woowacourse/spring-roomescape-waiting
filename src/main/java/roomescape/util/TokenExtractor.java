package roomescape.util;

import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Objects;
import javax.naming.AuthenticationException;
import roomescape.login.dto.TokenResponse;

public class TokenExtractor {

    private TokenExtractor() {
    }

    public static TokenResponse extractTokenFromCookie(Cookie[] cookies) throws AuthenticationException {
        validateNull(cookies);
        return Arrays.stream(cookies)
                .filter(cookie -> Objects.equals(cookie.getName(), "token"))
                .findFirst()
                .map(optionalToken -> new TokenResponse(optionalToken.getValue()))
                .orElseThrow(() -> new AuthenticationException("접근 권한 확인을 위한 쿠키가 없습니다."));
    }

    private static void validateNull(Cookie[] cookies) throws AuthenticationException {
        if (cookies == null) {
            throw new AuthenticationException("접근 권한 확인을 위한 쿠키가 없습니다.");
        }
    }
}
