package roomescape.auth.infrastructure.methodargument;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.infrastructure.AuthorizationPayload;
import roomescape.auth.infrastructure.handler.AuthorizationHandler;
import roomescape.auth.infrastructure.provider.AuthorizationProvider;

@Component
public class AuthorizationPrincipalInterceptor implements HandlerInterceptor {
    private final AuthorizationHandler authorizationHandler;
    private final AuthorizationProvider authorizationProvider;

    public AuthorizationPrincipalInterceptor(
        AuthorizationHandler authorizationHandler,
        AuthorizationProvider authorizationProvider
    ) {
        this.authorizationHandler = authorizationHandler;
        this.authorizationProvider = authorizationProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        authorizationHandler.getPrincipal(request).ifPresentOrElse(
            principal -> setPayload(request, authorizationProvider.getPayload(principal)),
            () -> setPayload(request, null)
        );
        return true;
    }

    private void setPayload(HttpServletRequest request, AuthorizationPayload payload) {
        request.setAttribute("authorizationPayload", payload);
    }
}
