package roomescape.infrastructure.auth;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;
import roomescape.application.TokenProvider;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@Component
public class JwtTokenProvider implements TokenProvider {
    private final JwtTokenProperties jwtTokenProperties;

    public JwtTokenProvider(JwtTokenProperties jwtTokenProperties) {
        this.jwtTokenProperties = jwtTokenProperties;
    }

    public String createToken(String payload) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtTokenProperties.getExpireMilliseconds());

        String secretKey = jwtTokenProperties.getSecretKey();
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(String.valueOf(payload))
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    public String getPayload(String token) {
        String secretString = jwtTokenProperties.getSecretKey();
        SecretKey key = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));

        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            throw new RoomescapeException(RoomescapeErrorCode.TOKEN_EXPIRED);
        }
    }
}
