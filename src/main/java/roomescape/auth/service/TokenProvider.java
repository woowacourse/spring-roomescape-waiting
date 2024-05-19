package roomescape.auth.service;

import roomescape.auth.domain.Payload;

public interface TokenProvider {
    String createAccessToken(String payload);

    Payload<String> getPayload(String token);

    boolean isToken(String token);
}
