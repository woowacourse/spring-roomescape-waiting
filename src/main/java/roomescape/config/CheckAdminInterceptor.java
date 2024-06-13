package roomescape.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.Role;
import roomescape.exception.AuthenticationException;
import roomescape.exception.AuthorizationException;
import roomescape.service.AuthService;

public class CheckAdminInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public CheckAdminInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getCookies() == null) {
            throw new AuthenticationException("인증되지 않은 사용자입니다.");
        }

        if (authService.isRoleValid(request.getCookies(), Role.ADMIN)) {
            return true;
        }

        throw new AuthorizationException("접근권한이 없습니다.");
    }
}
