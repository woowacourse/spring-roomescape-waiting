package roomescape.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import roomescape.auth.infrastructure.jwt.JwtProperties;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfiguration {
}
