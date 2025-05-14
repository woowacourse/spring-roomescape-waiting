package roomescape.presentation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.AuthenticationTokenHandler;

public class CheckAdminInterceptor implements HandlerInterceptor {

    private final AuthenticationTokenHandler tokenProvider;

    public CheckAdminInterceptor(final AuthenticationTokenHandler tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        if (isCurrentRequestorAdmin(request)) {
            return true;
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return false;
    }

    private boolean isCurrentRequestorAdmin(final HttpServletRequest request) {
        var tokenCookie = AuthenticationTokenCookie.fromRequest(request);
        if (tokenCookie.hasToken()) {
            var token = tokenCookie.token();
            return isAdmin(token);
        }
        return false;
    }

    private boolean isAdmin(final String token) {
        if (!tokenProvider.isValidToken(token)) {
            return false;
        }
        var authenticationInfo = tokenProvider.extractAuthenticationInfo(token);
        return authenticationInfo.isAdmin();
    }
}
