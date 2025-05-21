package roomescape.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt.token")
public class JwtProperties {

    private String secretKey;
    private long expireLength;

    public String getSecretKey() {
        return secretKey;
    }

    public long getExpireLength() {
        return expireLength;
    }

    public void setSecretKey(final String secretKey) {
        this.secretKey = secretKey;
    }

    public void setExpireLength(final long expireLength) {
        this.expireLength = expireLength;
    }
}
