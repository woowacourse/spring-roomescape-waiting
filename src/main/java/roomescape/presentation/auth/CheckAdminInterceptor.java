package roomescape.presentation.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.auth.AuthenticationInfo;
import roomescape.domain.auth.AuthenticationTokenHandler;

public class CheckAdminInterceptor implements HandlerInterceptor {

    private final AuthenticationTokenHandler tokenProvider;

    public CheckAdminInterceptor(final AuthenticationTokenHandler tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean preHandle(@NonNull final HttpServletRequest request,
                             @NonNull final HttpServletResponse response,
                             @NonNull final Object handler) {
        if (isCurrentRequestorAdmin(request)) {
            return true;
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return false;
    }

    private boolean isCurrentRequestorAdmin(final HttpServletRequest request) {
        AuthenticationTokenCookie tokenCookie = AuthenticationTokenCookie.fromRequest(request);
        if (tokenCookie.hasToken()) {
            String token = tokenCookie.token();
            return isAdmin(token);
        }
        return false;
    }

    private boolean isAdmin(final String token) {
        if (!tokenProvider.isValidToken(token)) {
            return false;
        }
        AuthenticationInfo authenticationInfo = tokenProvider.extractAuthenticationInfo(token);
        return authenticationInfo.isAdmin();
    }
}
