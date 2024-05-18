package roomescape.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("security.jwt.token")
public record TokenProperties(String secretKey, Long expireLength) {
}
