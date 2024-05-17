package roomescape.web.auth;

import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Optional;

public class CookieHandler {

    private static final String COOKIE_NAME = "token";
    private static final String COOKIE_PATH = "/";

    private CookieHandler() {
    }

    public static Cookie createCookieByToken(String token) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setPath(COOKIE_PATH);
        return cookie;
    }

    public static String extractTokenFromCookies(Cookie[] cookies) {
        Optional<String> token = Arrays.stream(cookies)
            .filter(cookie -> cookie.getName().equals(COOKIE_NAME))
            .map(Cookie::getValue)
            .findFirst();

        return token.orElseThrow(() -> new IllegalArgumentException("토큰이 존재하지 않습니다."));
    }
}
