package roomescape.global.ratelimit;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({InboundRateLimitProperties.class, OutboundRateLimitProperties.class})
public class RateLimitConfig {
}
