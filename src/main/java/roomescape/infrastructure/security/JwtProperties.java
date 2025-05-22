package roomescape.infrastructure.security;

import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import javax.crypto.SecretKey;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("security.jwt.token")
public class JwtProperties {

    private final SecretKey secretKey;
    private final Duration expireDuration;

    public JwtProperties(String secretKey, Duration expireDuration) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.expireDuration = expireDuration;
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }

    public Duration getExpireDuration() {
        return expireDuration;
    }
}
