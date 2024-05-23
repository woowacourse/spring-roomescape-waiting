package roomescape.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.infrastructure.TokenProvider;

@Component
public class MemberLoginInterceptor implements HandlerInterceptor {

    private final TokenProvider tokenProvider;

    public MemberLoginInterceptor(final TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        if (tokenProvider.doesNotRequestHasCookie(request) || tokenProvider.doesNotRequestHasToken(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        return true;
    }
}
