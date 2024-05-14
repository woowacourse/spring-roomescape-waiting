package roomescape.infra;

import static roomescape.exception.ExceptionType.INVALID_TOKEN;

import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import roomescape.exception.RoomescapeException;

public class TokenExtractor {
    public static String extractFrom(Cookie[] cookies) {
        if (cookies == null) {
            throw new RoomescapeException(INVALID_TOKEN);
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("token"))
                .limit(1)
                .findAny()
                .map(Cookie::getValue)
                .orElseThrow(() -> new RoomescapeException(INVALID_TOKEN));
    }
}
