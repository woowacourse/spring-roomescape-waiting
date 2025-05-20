package roomescape.auth.jwt.manager;

import io.jsonwebtoken.Claims;
import roomescape.auth.jwt.domain.Jwt;
import roomescape.auth.jwt.domain.TokenType;

public interface JwtManager {

    Jwt generate(Claims claims, TokenType type);

    Claims parse(Jwt token);
}
