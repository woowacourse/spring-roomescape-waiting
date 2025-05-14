package roomescape.auth.infrastructure.provider;

import roomescape.auth.infrastructure.AuthorizationPayload;
import roomescape.auth.infrastructure.AuthorizationPrincipal;

public interface AuthorizationProvider {
    AuthorizationPrincipal createPrincipal(AuthorizationPayload payload);

    AuthorizationPayload getPayload(AuthorizationPrincipal principal);

    void validatePrincipal(AuthorizationPrincipal principal);
}
