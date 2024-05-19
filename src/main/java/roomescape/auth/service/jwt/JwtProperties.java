package roomescape.auth.service.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt.token")
public class JwtProperties {
    private final String secretKey;

    private final long expireLength;

    public JwtProperties(String secretKey, long expireLength) {
        this.secretKey = secretKey;
        this.expireLength = expireLength;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public long getExpireLength() {
        return expireLength;
    }
}
