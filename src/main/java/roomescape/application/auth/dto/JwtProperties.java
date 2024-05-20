package roomescape.application.auth.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private long expireInMillis;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpireInMillis() {
        return expireInMillis;
    }

    public void setExpireInMillis(long expireInMillis) {
        this.expireInMillis = expireInMillis;
    }
}
