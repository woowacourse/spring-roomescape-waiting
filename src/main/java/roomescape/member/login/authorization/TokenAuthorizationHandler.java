package roomescape.member.login.authorization;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import roomescape.common.exception.AuthorizationException;
import roomescape.common.exception.message.LoginExceptionMessage;

@Component
public class TokenAuthorizationHandler implements AuthorizationHandler<String> {

    private static final String AUTHORIZATION = "token";

    @Override
    public String extractToken(HttpServletRequest httpServletRequest) {
        Cookie[] cookies = httpServletRequest.getCookies();
        validateCookie(cookies);
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(AUTHORIZATION)) {
                return cookie.getValue();
            }
        }
        throw new AuthorizationException(LoginExceptionMessage.INVALID_TOKEN_VALUE.getMessage());
    }

    @Override
    public void createCookie(String accessToken, HttpServletResponse httpServletResponse) {
        Cookie cookie = new Cookie("token", accessToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        httpServletResponse.addCookie(cookie);
    }

    @Override
    public void deleteCookie(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Cookie[] cookies = httpServletRequest.getCookies();
        validateCookie(cookies);
        for (Cookie cookie : cookies) {
            cookie.setValue("");
            cookie.setPath("/");
            cookie.setMaxAge(0);
            httpServletResponse.addCookie(cookie);
        }
    }

    @Override
    public void validateCookie(Cookie[] cookies) {
        if (cookies == null) {
            throw new AuthorizationException(LoginExceptionMessage.INVALID_COOKIE.getMessage());
        }
    }
}
