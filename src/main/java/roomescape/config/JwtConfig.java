package roomescape.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import roomescape.auth.token.TokenProvider;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {

    @Value("${custom.security.jwt.token-secret-key}")
    private String tokenSecretKey;
    @Value("${custom.security.jwt.token-expiration-period}")
    private long tokenExpirationPeriod;

    @Bean
    public TokenProvider tokenProvider() {
        final SecretKey secretKey = Keys.hmacShaKeyFor(tokenSecretKey.getBytes(StandardCharsets.UTF_8));
        return new TokenProvider(secretKey, tokenExpirationPeriod);
    }
}
