package roomescape.member.login.authorization;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthorizationHandler<T> {

    T extractToken(HttpServletRequest request);

    void createCookie(String accessToken, HttpServletResponse response);

    void deleteCookie(HttpServletRequest request, HttpServletResponse response);

    void validateCookie(Cookie[] cookies);
}
