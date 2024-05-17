package roomescape.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.controller.exception.AuthorizationException;
import roomescape.domain.Role;
import roomescape.infrastructure.TokenExtractor;
import roomescape.service.AuthService;

public class CheckAdminInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public CheckAdminInterceptor(final AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response, final Object handler) {
        final String token = TokenExtractor.fromRequest(request);
        final Role role = authService.findMemberRoleByToken(token);
        if (role.isAdmin()) {
            return true;
        }
        throw new AuthorizationException("어드민만 접근할 수 있습니다.");
    }
}
