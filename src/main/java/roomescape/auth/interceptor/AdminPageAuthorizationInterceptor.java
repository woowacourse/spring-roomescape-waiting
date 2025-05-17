package roomescape.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.AuthorizationExtractor;
import roomescape.domain.Role;
import roomescape.infrastructure.JwtTokenProvider;

public class AdminPageAuthorizationInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthorizationExtractor authorizationExtractor;

    public AdminPageAuthorizationInterceptor(
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
            response.sendRedirect("/");
            return false;
        }
        try {
            if (Role.ADMIN != jwtTokenProvider.extractRole(token.get())) {
                response.sendRedirect("/");
                return false;
            }
        } catch (Exception e) {
            response.sendRedirect("/");
            return false;
        }
        return true;
    }
}
