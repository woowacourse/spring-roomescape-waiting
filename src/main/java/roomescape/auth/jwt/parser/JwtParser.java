package roomescape.auth.jwt.parser;

import io.jsonwebtoken.Claims;
import roomescape.auth.jwt.domain.Jwt;

import javax.crypto.SecretKey;

public interface JwtParser {

    Claims execute(Jwt token, SecretKey secretKey);
}
