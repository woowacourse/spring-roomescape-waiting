package roomescape.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.global.exception.ForbiddenException;
import roomescape.auth.service.AuthService;

public class AdminInterceptor implements HandlerInterceptor {

    private AuthService authService;

    public AdminInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (authService.isAdminRequest(request.getCookies())) {
            return true;
        }
        throw new ForbiddenException("관리자만 접근 가능한 페이지이다.");
    }
}
