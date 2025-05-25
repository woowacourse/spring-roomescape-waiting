package roomescape.presentation.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.LoginMember;
import roomescape.domain.Role;
import roomescape.presentation.support.CookieUtils;
import roomescape.service.AuthService;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;
    private final CookieUtils cookieUtils;

    public AdminAuthInterceptor(AuthService authService, CookieUtils cookieUtils) {
        this.authService = authService;
        this.cookieUtils = cookieUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (!cookieUtils.containsCookieForToken(request)) {
            response.setStatus(401);
            return false;
        }

        String token = cookieUtils.getToken(request);

        LoginMember loginMember = authService.getMemberByToken(token);
        if (Role.isAdmin(loginMember.getRole())) {
            return true;
        }
        response.setStatus(401);
        return false;
    }
}
