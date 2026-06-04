package roomescape.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.Role;
import roomescape.exception.UnauthenticatedException;
import roomescape.exception.UnauthorizedException;

@Component
public class AdminAuthorizationInterceptor implements HandlerInterceptor {

    private final AuthorizationExtractor<String> authorizationExtractor = new BearerAuthorizationExtractor();
    private final JwtTokenProvider jwtProvider;

    public AdminAuthorizationInterceptor(JwtTokenProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        String token = authorizationExtractor.extract(request);
        if (token == null) {
            throw new UnauthenticatedException();
        }
        Role role = jwtProvider.getRole(token);
        if (role != Role.MANAGER) {
            throw new UnauthorizedException();
        }
        request.setAttribute(LoginCheckInterceptor.LOGIN_USER_ID, jwtProvider.getUserId(token));
        return true;
    }
}
