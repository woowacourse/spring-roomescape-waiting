package roomescape.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt.token")
public record TokenProperties(String secretKey, long expireLength) {
}
