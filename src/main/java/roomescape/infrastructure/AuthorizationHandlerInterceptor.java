package roomescape.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthorizationHandlerInterceptor implements HandlerInterceptor {
    private final TokenProvider tokenProvider;

    public AuthorizationHandlerInterceptor(final TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        final String token = tokenProvider.getTokenFromCookies(request);

        tokenProvider.validateTokenExpiration(token);
        tokenProvider.validateTokenRole(token);

        return true;
    }
}
