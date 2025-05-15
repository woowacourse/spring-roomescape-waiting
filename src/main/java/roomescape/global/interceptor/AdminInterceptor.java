package roomescape.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.infrastructure.JwtExtractor;
import roomescape.auth.infrastructure.JwtTokenProvider;
import roomescape.auth.model.Principal;
import roomescape.global.exception.ForbiddenException;
import roomescape.auth.service.AuthService;

public class AdminInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    public AdminInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String accessToken = JwtExtractor.extractTokenFromCookie(request.getCookies());
        Principal principal = jwtTokenProvider.resolvePrincipalFromToken(accessToken);
        if (principal.isAdmin()) {
            return true;
        }
        throw  new ForbiddenException();
    }
}
