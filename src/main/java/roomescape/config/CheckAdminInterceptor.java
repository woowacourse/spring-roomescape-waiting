package roomescape.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.Role;
import roomescape.infrastructure.TokenProvider;

@Component
public class CheckAdminInterceptor implements HandlerInterceptor {

    private final TokenProvider tokenProvider;

    public CheckAdminInterceptor(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Role role = tokenProvider.parseAuthenticationRoleFromCookies(request.getCookies());
        if (role.isNotAdmin()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        return true;
    }
}
