package roomescape.service.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt.token")
public class JwtProperties {

    private final String secretKey;
    private final long expirationMillis;

    public JwtProperties(String secretKey, long expirationMillis) {
        this.secretKey = secretKey;
        this.expirationMillis = expirationMillis;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public long getExpirationMillis() {
        return expirationMillis;
    }
}
