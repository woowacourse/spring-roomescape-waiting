package roomescape.auth.domain;

import java.time.Instant;

public interface AuthTokenProvider {

    String createAccessToken(String principal, AuthRole role);

    String getPrincipal(String token);

    Instant getExpiration(String token);

    AuthRole getRole(String token);

    boolean validateToken(String token);
}
