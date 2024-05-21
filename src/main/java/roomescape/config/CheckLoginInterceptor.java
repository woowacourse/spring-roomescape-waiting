package roomescape.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.infrastructure.TokenExtractor;
import roomescape.service.AuthService;
import roomescape.service.exception.InvalidTokenException;

public class CheckLoginInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public CheckLoginInterceptor(final AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response, final Object handler)
            throws IOException {
        final String token = TokenExtractor.fromRequest(request);
        if (token == null) {
            return redirectToLoginPage(response);
        }
        try {
            authService.validateToken(token);
        } catch (final InvalidTokenException e) {
            return redirectToLoginPage(response);
        }
        return true;
    }

    private boolean redirectToLoginPage(HttpServletResponse response) throws IOException {
        response.sendRedirect("/login");
        return false;
    }
}
