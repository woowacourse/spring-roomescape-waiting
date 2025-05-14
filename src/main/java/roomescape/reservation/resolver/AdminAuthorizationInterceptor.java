package roomescape.reservation.resolver;

import static roomescape.member.role.Role.ADMIN;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.service.AuthService;

public class AdminAuthorizationInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public AdminAuthorizationInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = authService.extractToken(request);

        MemberResponse member = authService.findUserByToken(token);

        if (!ADMIN.getRole().equals(member.role())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        return true;
    }
}
