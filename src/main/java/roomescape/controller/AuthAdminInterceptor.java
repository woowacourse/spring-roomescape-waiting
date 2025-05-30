package roomescape.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.controller.annotation.AdminOnly;
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
        if (request.getRequestURI().startsWith("/admin") ||
                (handler instanceof HandlerMethod handlerMethod && (
                        handlerMethod.getMethodAnnotation(AdminOnly.class) != null ||
                        handlerMethod.getBeanType().isAnnotationPresent(AdminOnly.class))
                )
        ) {
            validateMemberAdminIfTokenExists(request);
        }
        return true;
    }

    private void validateMemberAdminIfTokenExists(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String token = cookieHandler.extractCookie(cookies, TOKEN_COOKIE_NAME);
        Role role = jwtTokenProvider.extractRole(token);
        role.checkAdminAccess();
    }
}
