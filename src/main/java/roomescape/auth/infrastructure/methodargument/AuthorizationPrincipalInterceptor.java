package roomescape.auth.infrastructure.methodargument;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.infrastructure.AuthorizationPayload;
import roomescape.auth.infrastructure.AuthorizationPrincipal;
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
        Optional<AuthorizationPrincipal> authorizationPrincipal = authorizationHandler.get(request);
        if (authorizationPrincipal.isEmpty()) {
            System.out.println("authorizationPrincipal = " + authorizationPrincipal);
            request.setAttribute("authorizationPayload", null);
            return true;
        }
        authorizationProvider.validatePrincipal(authorizationPrincipal.get());
        AuthorizationPayload authorizationPayload = authorizationProvider.getPayload(authorizationPrincipal.get());
        request.setAttribute("authorizationPayload", authorizationPayload);
        return true;
    }
}
