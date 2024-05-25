package roomescape.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.controller.exception.AuthorizationException;
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
        final Optional<String> token = TokenExtractor.fromRequest(request);
        final boolean isAdmin = token.map(authService::IsMemberAdminByToken).orElse(false);
        if (isAdmin) {
            return true;
        }
        throw new AuthorizationException("어드민만 접근할 수 있습니다.");
    }
}
