package roomescape.application.auth;

import roomescape.application.auth.dto.TokenPayload;

public interface TokenManager {

    String createToken(TokenPayload payload);

    TokenPayload extract(String token);
}
