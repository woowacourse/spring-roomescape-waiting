package roomescape.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.infrastructure.TokenExtractor;
import roomescape.service.AuthService;

public class CheckLoginInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public CheckLoginInterceptor(final AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response, final Object handler)
            throws IOException {
        final Optional<String> token = TokenExtractor.fromRequest(request);
        final boolean isValidLogin = token.filter(authService::isValidToken)
                .isPresent();
        if (!isValidLogin) {
            response.sendRedirect("/login");
        }
        return isValidLogin;
    }
}
