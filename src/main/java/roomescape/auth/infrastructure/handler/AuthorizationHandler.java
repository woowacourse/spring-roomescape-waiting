package roomescape.auth.infrastructure.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import roomescape.auth.infrastructure.AuthorizationPrincipal;

public interface AuthorizationHandler {
    Optional<AuthorizationPrincipal> get(HttpServletRequest request);

    void set(HttpServletResponse response, AuthorizationPrincipal principal);

    void remove(HttpServletResponse response);
}
