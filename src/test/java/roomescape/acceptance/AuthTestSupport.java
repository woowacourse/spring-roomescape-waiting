package roomescape.acceptance;

import roomescape.domain.Role;
import roomescape.infrastructure.JwtTokenProvider;

public final class AuthTestSupport {

    private static final String SECRET_KEY = "/BWxvVt/eMsTVSq+RI9kRCrZKK38KNGIWi7ilxCg9So=";
    private static final long EXPIRE_LENGTH = 3600000L;
    private static final JwtTokenProvider PROVIDER = new JwtTokenProvider(SECRET_KEY, EXPIRE_LENGTH);

    private AuthTestSupport() {
    }

    public static String token(long userId, String username, Role role) {
        return PROVIDER.createToken(userId, username, role);
    }

    public static String bearer(long userId, String username, Role role) {
        return "Bearer " + token(userId, username, role);
    }
}
