package roomescape.infra;

import static roomescape.exception.ExceptionType.INVALID_TOKEN;

import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import roomescape.exception.RoomescapeException;

public class TokenExtractor {

    public static final int VALID_TOKEN_COOKIE_COUNT = 1;

    public static String extractFrom(Cookie[] cookies) {
        if (cookies == null) {
            throw new RoomescapeException(INVALID_TOKEN);
        }
        validateTokenCookieCount(cookies);
        return Arrays.stream(cookies)
                .filter(TokenExtractor::isTokenCookie)
                .limit(VALID_TOKEN_COOKIE_COUNT)
                .findAny()
                .map(Cookie::getValue)
                .orElseThrow(() -> new RoomescapeException(INVALID_TOKEN));
    }

    private static void validateTokenCookieCount(Cookie[] cookies) {
        long tokenCookieCount = Arrays.stream(cookies)
                .filter(TokenExtractor::isTokenCookie)
                .count();
        if (tokenCookieCount != VALID_TOKEN_COOKIE_COUNT) {
            throw new RoomescapeException(INVALID_TOKEN);
        }
    }

    private static boolean isTokenCookie(Cookie cookie) {
        return cookie.getName().equals("token");
    }
}
