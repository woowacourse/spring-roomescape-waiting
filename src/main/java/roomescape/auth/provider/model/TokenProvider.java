package roomescape.auth.provider.model;

import roomescape.auth.domain.Token;

public interface TokenProvider {

    Token getAccessToken(long principal);

    String resolveToken(String token);
}
