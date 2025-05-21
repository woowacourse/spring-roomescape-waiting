package roomescape.global.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.service.AuthService;

public class AdminCheckInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public AdminCheckInterceptor(final AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(
            final HttpServletRequest request,
            final HttpServletResponse response, final Object handler
    ) throws Exception {
        final String token = CookieUtil.parseCookie(request.getCookies());
        authService.checkAdminMember(token);
        return true;
    }
}
