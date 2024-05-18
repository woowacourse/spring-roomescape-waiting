package roomescape.auth.handler.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import roomescape.auth.handler.RequestHandler;
import roomescape.exception.custom.UnauthorizedException;


@Component
public class CookieRequestHandler implements RequestHandler {

    private static final String SESSION_KEY = "token";

    @Override
    public String extract(HttpServletRequest request) {
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(SESSION_KEY)) {
                return cookie.getValue();
            }
        }
        throw new UnauthorizedException("유효하지 않는 토큰입니다.");
    }
}
