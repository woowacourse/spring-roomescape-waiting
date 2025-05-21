package roomescape.auth.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import roomescape.auth.annotation.RequiredAdmin;
import roomescape.auth.service.JwtTokenHandler;
import roomescape.common.AuthTokenCookieProvider;
import roomescape.common.exception.AuthenticationException;
import roomescape.member.domain.Role;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    private final JwtTokenHandler jwtTokenHandler;
    private final AuthTokenCookieProvider authTokenCookieProvider;

    public AdminInterceptor(final JwtTokenHandler jwtTokenHandler, AuthTokenCookieProvider authTokenCookieProvider) {
        this.jwtTokenHandler = jwtTokenHandler;
        this.authTokenCookieProvider = authTokenCookieProvider;
    }

    @Override
    public boolean preHandle(
            final HttpServletRequest request, final HttpServletResponse response, final Object handler
    ) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        if (canAccessAnyone(handlerMethod)) {
            return true;
        }
        return hasAdminRole(request);
    }

    private boolean canAccessAnyone(final HandlerMethod handlerMethod) {
        return handlerMethod.getMethodAnnotation(RequiredAdmin.class) == null;
    }

    private boolean hasAdminRole(final HttpServletRequest request) {
        String token = authTokenCookieProvider.extractToken(request);
        Role role = jwtTokenHandler.getRole(token);
        if (role == Role.ADMIN) {
            return true;
        }
        throw new AuthenticationException("권한이 존재하지 않습니다.");
    }
}
