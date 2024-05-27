package roomescape.application.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Clock;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.application.auth.dto.TokenPayload;
import roomescape.domain.member.Role;
import roomescape.exception.ExpiredTokenException;
import roomescape.exception.InvalidTokenException;

@Component
public class JwtTokenManager implements TokenManager {
    public static final String CLAIM_NAME = "name";
    public static final String CLAIM_ROLE = "role";

    private final Algorithm secretAlgorithm;
    private final long tokenExpirationMills;
    private final Clock clock;

    public JwtTokenManager(@Value("${jwt.secret}") String secret,
                           @Value("${jwt.expire-in-millis}") long tokenExpirationMills,
                           Clock clock) {
        this.secretAlgorithm = Algorithm.HMAC512(secret);
        this.tokenExpirationMills = tokenExpirationMills;
        this.clock = clock;
    }

    @Override
    public String createToken(TokenPayload payload) {
        Date now = Date.from(clock.instant());
        Date expiresAt = new Date(now.getTime() + tokenExpirationMills);
        return JWT.create()
                .withSubject(String.valueOf(payload.memberId()))
                .withClaim(CLAIM_NAME, payload.name())
                .withClaim(CLAIM_ROLE, payload.roleName())
                .withExpiresAt(expiresAt)
                .sign(secretAlgorithm);
    }

    @Override
    public TokenPayload extract(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            return getTokenPayload(decodedJWT);
        } catch (JWTDecodeException e) {
            throw new InvalidTokenException(e);
        }
    }

    private TokenPayload getTokenPayload(DecodedJWT decodedJWT) {
        validateNotExpired(decodedJWT);
        try {
            long memberId = Long.parseLong(decodedJWT.getSubject());
            String name = decodedJWT.getClaim("name").asString();
            String roleName = decodedJWT.getClaim("role").asString();
            return new TokenPayload(memberId, name, Role.from(roleName));
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException(e);
        }
    }

    private void validateNotExpired(DecodedJWT decodedJWT) {
        Date now = Date.from(clock.instant());
        Date expiresAt = decodedJWT.getExpiresAt();
        if (expiresAt == null) {
            throw new InvalidTokenException();
        }
        if (expiresAt.before(now)) {
            throw new ExpiredTokenException();
        }
    }
}
