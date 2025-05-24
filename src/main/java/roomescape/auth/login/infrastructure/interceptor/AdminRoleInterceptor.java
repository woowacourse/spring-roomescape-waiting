package roomescape.auth.login.infrastructure.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.exception.ForbiddenException;
import roomescape.auth.exception.UnauthorizedException;
import roomescape.auth.token.exception.JwtExtractException;
import roomescape.auth.token.JwtTokenManager;
import roomescape.auth.token.TokenExtractor;

@Component
public class AdminRoleInterceptor implements HandlerInterceptor {

    public static final String ADMIN_STRING = "ADMIN";

    private final JwtTokenManager jwtTokenManager;

    public AdminRoleInterceptor(JwtTokenManager jwtTokenManager) {
        this.jwtTokenManager = jwtTokenManager;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = TokenExtractor.extract(request);

        try {
            String role = jwtTokenManager.getRole(token);
            validateRoleIsAdmin(role);

            return true;
        } catch (final JwtExtractException exception) {
            throw new UnauthorizedException(exception.getMessage());
        }
    }

    private static void validateRoleIsAdmin(final String role) {
        if (!role.equals(ADMIN_STRING)) {
            throw new ForbiddenException("관리자가 아닙니다.");
        }
    }
}
