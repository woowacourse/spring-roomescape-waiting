package roomescape.auth.infrastructure.provider.token;

import roomescape.auth.infrastructure.AuthorizationPayload;
import roomescape.auth.infrastructure.AuthorizationPrincipal;
import roomescape.auth.infrastructure.provider.AuthorizationProvider;

public abstract class TokenAuthorizationProvider implements AuthorizationProvider {

    @Override
    public final AuthorizationPrincipal createPrincipal(AuthorizationPayload payload) {
        String token = createToken(
            new AuthorizationPayload(payload.name(), payload.role())
        );
        return new AuthorizationPrincipal(token);
    }

    @Override
    public final AuthorizationPayload getPayload(AuthorizationPrincipal principal) {
        String token = principal.value();
        AuthorizationPayload payload = getPayload(token);
        return new AuthorizationPayload(payload.name(), payload.role());
    }

    @Override
    public final void validatePrincipal(AuthorizationPrincipal principal) {
        String token = principal.value();
        validateToken(token);
    }

    protected abstract String createToken(AuthorizationPayload payload);

    protected abstract AuthorizationPayload getPayload(String token);

    protected abstract void validateToken(String token);
}
