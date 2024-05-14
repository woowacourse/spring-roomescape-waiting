package roomescape.service;

import io.jsonwebtoken.Claims;
import roomescape.model.Member;

public interface TokenProvider {
    String createToken(Member member);

    Claims getPayload(String token);
}
