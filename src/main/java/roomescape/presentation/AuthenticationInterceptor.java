package roomescape.presentation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.application.AuthService;
import roomescape.application.exception.AuthException;
import roomescape.domain.Role;
import roomescape.infrastructure.util.JwtTokenExtractor;

public class AuthenticationInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public AuthenticationInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        String token = JwtTokenExtractor.extract(request);

        Role role = authService.findRoleByToken(token);
        if (role != Role.ADMIN) {
            throw new AuthException("[ERROR] 권한이 필요합니다.", HttpStatus.FORBIDDEN);
        }
        return true;
    }
}
