package roomescape.global.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import roomescape.auth.infrastructure.Token;
import roomescape.global.exception.IllegalRequestException;

public class CookieUtils {

    private static final String TOKEN_NAME = "token";

    private CookieUtils() {
    }

    public static String getToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new IllegalRequestException("로그인 되어 있지 않습니다");
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(TOKEN_NAME))
                .map(Cookie::getValue)
                .findAny()
                .orElseThrow(() -> new IllegalRequestException("로그인 되어 있지 않습니다"));
    }

    public static Cookie createTokenCookie(Token token) {
        Cookie cookie = new Cookie(TOKEN_NAME, token.getAccessToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    public static void clearTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(TOKEN_NAME, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
