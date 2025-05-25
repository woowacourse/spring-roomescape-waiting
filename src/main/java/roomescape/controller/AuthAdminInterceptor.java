package roomescape.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.controller.util.CookieHandler;
import roomescape.domain.member.Role;
import roomescape.util.JwtTokenProvider;

@Component
public class AuthAdminInterceptor implements HandlerInterceptor {

    private static final String TOKEN_COOKIE_NAME = "token";

    private final JwtTokenProvider jwtTokenProvider;
    private final CookieHandler cookieHandler;

    public AuthAdminInterceptor(JwtTokenProvider jwtTokenProvider, CookieHandler cookieHandler) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.cookieHandler = cookieHandler;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Cookie[] cookies = request.getCookies();
        String token = cookieHandler.extractCookie(cookies, TOKEN_COOKIE_NAME);
        Role role = jwtTokenProvider.extractRole(token);
        role.checkAdminAccess();
        return true;
    }
}
