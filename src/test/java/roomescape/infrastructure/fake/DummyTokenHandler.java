package roomescape.infrastructure.fake;

import roomescape.domain.auth.AuthenticationInfo;
import roomescape.domain.auth.AuthenticationTokenHandler;
import roomescape.domain.user.UserRole;

public class DummyTokenHandler implements AuthenticationTokenHandler {

    private static final String SEPARATOR = ";";

    @Override
    public String createToken(final AuthenticationInfo authenticationInfo) {
        return authenticationInfo.id() + SEPARATOR + authenticationInfo.role().name();
    }

    @Override
    public long extractId(final String token) {
        var authenticationInfo = extractAuthenticationInfo(token);
        return authenticationInfo.id();
    }

    @Override
    public AuthenticationInfo extractAuthenticationInfo(final String token) {
        var split = token.split(SEPARATOR);
        var id = Long.parseLong(split[0]);
        var role = UserRole.valueOf(split[1]);
        return new AuthenticationInfo(id, role);
    }

    @Override
    public boolean isValidToken(final String token) {
        return true;
    }
}
