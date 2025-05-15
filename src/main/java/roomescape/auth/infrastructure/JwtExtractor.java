package roomescape.auth.infrastructure;

import jakarta.servlet.http.Cookie;
import roomescape.auth.constant.AuthConstant;
import roomescape.global.exception.UnauthorizedException;

public class JwtExtractor {

    public static String extractTokenFromCookie(Cookie[] cookies) {
        if (cookies == null) {
            throw new UnauthorizedException();
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(AuthConstant.COOKIE_KEY_OF_ACCESS_TOKEN)) {
                return cookie.getValue();
            }
        }
        throw new UnauthorizedException();
    }
}
