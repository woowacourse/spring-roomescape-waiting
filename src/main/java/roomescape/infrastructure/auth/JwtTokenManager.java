package roomescape.infrastructure.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Objects;
import org.springframework.stereotype.Component;
import roomescape.application.TokenManager;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@Component
public class JwtTokenManager implements TokenManager {
    private static final String TOKEN_KEY = "token";
    private static final int ONE_MINUTE = 60;

    @Override
    public String extractToken(Cookie[] cookies) {
        if (cookies == null || Arrays.stream(cookies).anyMatch(Objects::isNull)) {
            throw new RoomescapeException(RoomescapeErrorCode.UNAUTHORIZED);
        }
        return getAccessToken(cookies);
    }

    private String getAccessToken(Cookie[] cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(TOKEN_KEY))
                .findAny()
                .map(Cookie::getValue)
                .orElseThrow(() -> new RoomescapeException(RoomescapeErrorCode.UNAUTHORIZED, "토큰이 존재하지 않습니다."));
    }

    @Override
    public void setToken(HttpServletResponse response, String accessToken) {
        Cookie cookie = new Cookie(TOKEN_KEY, accessToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(ONE_MINUTE * 5);
        response.addCookie(cookie);
    }
}
