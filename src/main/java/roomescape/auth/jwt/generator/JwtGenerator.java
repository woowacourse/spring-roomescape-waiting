package roomescape.auth.jwt.generator;

import io.jsonwebtoken.Claims;
import roomescape.auth.jwt.domain.Jwt;

import javax.crypto.SecretKey;
import java.time.Instant;

public interface JwtGenerator {

    Jwt execute(Claims claims, Instant now, Instant expiration, SecretKey secretKey);
}
