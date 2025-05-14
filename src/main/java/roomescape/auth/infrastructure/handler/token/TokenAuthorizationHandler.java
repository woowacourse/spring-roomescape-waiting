package roomescape.auth.infrastructure.handler.token;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import roomescape.auth.infrastructure.AuthorizationPrincipal;
import roomescape.auth.infrastructure.handler.AuthorizationHandler;

public abstract class TokenAuthorizationHandler implements AuthorizationHandler {

    @Override
    public final Optional<AuthorizationPrincipal> get(HttpServletRequest request) {
        String token = getToken(request);
        return Optional.ofNullable(token)
            .map(AuthorizationPrincipal::new);
    }

    @Override
    public final void set(HttpServletResponse response, AuthorizationPrincipal principal) {
        setToken(response, principal.value());
    }

    protected abstract String getToken(HttpServletRequest request);

    protected abstract void setToken(HttpServletResponse response, String token);
}
