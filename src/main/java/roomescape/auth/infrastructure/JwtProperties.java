package roomescape.auth.infrastructure;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("security.jwt.token")
@Getter
@RequiredArgsConstructor
public class JwtProperties {

    private final String secretKey;
    private final Long expireLength;
}
