package roomescape.presentation.support;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import roomescape.common.exception.UnauthorizedException;

@Component
public class CookieUtils {

    public static final String COOKIE_NAME_FOR_TOKEN = "token";

    public void setCookieForToken(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(COOKIE_NAME_FOR_TOKEN, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public String getToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            throw new UnauthorizedException("해당 경로로 접근할 권한이 없습니다.");
        }

        Cookie foundCookie = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(COOKIE_NAME_FOR_TOKEN))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("쿠키에 권한 정보가 포함되어 있지 않습니다."));

        return foundCookie.getValue();
    }

    public boolean containsCookieForToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return false;
        }
        return Arrays.stream(cookies)
                .anyMatch(cookie -> cookie.getName().equals("token"));
    }
}
