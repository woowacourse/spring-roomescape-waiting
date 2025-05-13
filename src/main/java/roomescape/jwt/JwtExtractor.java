package roomescape.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import roomescape.exception.UnauthorizedException;

public class JwtExtractor {

    private JwtExtractor() {
    }

    public static String extractFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new UnauthorizedException("쿠키가 비어있습니다.");
        }
        return extractTokenFromCookie(cookies);
    }

    private static String extractTokenFromCookie(Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("token")) {
                return cookie.getValue();
            }
        }
        throw new UnauthorizedException("쿠키에 토큰이 없습니다.");
    }
}
