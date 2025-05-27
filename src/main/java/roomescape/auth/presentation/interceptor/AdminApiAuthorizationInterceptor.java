package roomescape.auth.presentation.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.infrastructure.JwtTokenProvider;
import roomescape.auth.presentation.AuthorizationExtractor;
import roomescape.exception.AccessDeniedException;
import roomescape.exception.AuthenticationRequiredException;
import roomescape.member.domain.Role;

public class AdminApiAuthorizationInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthorizationExtractor authorizationExtractor;

    public AdminApiAuthorizationInterceptor(
            JwtTokenProvider jwtTokenProvider,
            AuthorizationExtractor authorizationExtractor
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authorizationExtractor = authorizationExtractor;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Optional<String> token = authorizationExtractor.extract(request);
        if (token.isEmpty()) {
            throw new AuthenticationRequiredException();
        }
        if (Role.ADMIN != jwtTokenProvider.extractRole(token.get())) {
            throw new AccessDeniedException();
        }
        return true;
    }
}
