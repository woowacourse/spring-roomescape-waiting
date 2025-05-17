package roomescape.infrastructure.security;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private final Clock clock;

    public JwtProvider(JwtProperties jwtProperties, Clock clock) {
        this.jwtProperties = jwtProperties;
        this.clock = clock;
    }

    public AccessToken issue(Long identifier) {
        TokenIssueRequest tokenIssueRequest = getTokenIssueRequest(identifier);
        return AccessToken.create(tokenIssueRequest);
    }

    private TokenIssueRequest getTokenIssueRequest(Long identifier) {
        Instant currentInstant = clock.instant();
        Instant expireInstant = currentInstant.plus(jwtProperties.getExpireDuration());
        Date currentDate = Date.from(currentInstant);
        Date expireDate = Date.from(expireInstant);
        return new TokenIssueRequest(
                currentDate,
                expireDate,
                identifier,
                jwtProperties.getSecretKey()
        );
    }

    public Long extractIdentifier(AccessToken accessToken) {
        return accessToken.extractMemberId(jwtProperties.getSecretKey());
    }
}
