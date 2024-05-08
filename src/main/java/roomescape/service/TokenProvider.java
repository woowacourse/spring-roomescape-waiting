package roomescape.service;

import io.jsonwebtoken.Claims;
import roomescape.model.User;

public interface TokenProvider {
    String createToken(User user);

    Claims getPayload(String token);
}
