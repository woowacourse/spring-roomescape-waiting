package roomescape.auth.infrastructure;

import io.jsonwebtoken.Claims;

public interface TokenProvider {

    String createToken(final Claims claims);

    String extractPrincipal(final String token);
}
