package roomescape.infrastructure;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Date;
import org.springframework.stereotype.Component;
import roomescape.domain.MemberRole;
import roomescape.dto.TokenInfo;

@Component
public class TokenGenerator {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_EMAIL = "email";

    private final JwtTokenProperties jwtTokenProperties;

    public TokenGenerator(JwtTokenProperties jwtTokenProperties) {
        this.jwtTokenProperties = jwtTokenProperties;
    }

    public String createToken(TokenInfo tokenInfo) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtTokenProperties.getExpireLength());

        return JWT.create()
                .withClaim(CLAIM_EMAIL, tokenInfo.payload())
                .withClaim(CLAIM_ROLE, tokenInfo.getRole())
                .withExpiresAt(validity)
                .sign(Algorithm.HMAC256(jwtTokenProperties.getSecretKey()));
    }

    public TokenInfo extract(String token) {
        DecodedJWT decodeJWT = JWT.decode(token);
        return getPayload(decodeJWT);
    }

    private TokenInfo getPayload(DecodedJWT decodedJWT) {
        validateTokenExpired(decodedJWT);
        try {
            String payload = decodedJWT.getClaim(CLAIM_EMAIL).asString();
            String role = decodedJWT.getClaim(CLAIM_ROLE).asString();
            return new TokenInfo(payload, MemberRole.from(role));
        } catch (IllegalArgumentException e) {
            throw new SecurityException("유효하지 않는 토큰입니다.");
        }
    }

    private void validateTokenExpired(DecodedJWT decodedJWT) {
        Date now = new Date();
        Date expiresAt = decodedJWT.getExpiresAt();
        if (expiresAt == null || expiresAt.before(now)) {
            throw new SecurityException("만료된 토큰입니다.");
        }
    }
}
