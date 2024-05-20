package roomescape.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "security.jwt.token")
public class JwtTokenProperties {

    private final String secretKey;
    private final long expireLength;

    @ConstructorBinding
    public JwtTokenProperties(String secretKey, long expireLength) {
        this.secretKey = secretKey;
        this.expireLength = expireLength;
    }

    public long getExpireLength() {
        return expireLength;
    }

    public String getSecretKey() {
        return secretKey;
    }
}
