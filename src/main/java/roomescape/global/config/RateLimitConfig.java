package roomescape.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import roomescape.global.ratelimit.OutboundRateLimitProperties;
import roomescape.global.ratelimit.RateLimitProperties;
import roomescape.global.ratelimit.TossRetryProperties;
import roomescape.global.ratelimit.TokenBucketRateLimiter;

@Configuration
@EnableConfigurationProperties({RateLimitProperties.class, OutboundRateLimitProperties.class, TossRetryProperties.class})
public class RateLimitConfig {

    @Bean("inboundRateLimiter")
    public TokenBucketRateLimiter inboundRateLimiter(RateLimitProperties properties) {
        return new TokenBucketRateLimiter(properties.capacity(), properties.refillPerSec(), System::nanoTime);
    }

    @Bean("outboundRateLimiter")
    public TokenBucketRateLimiter outboundRateLimiter(OutboundRateLimitProperties properties) {
        return new TokenBucketRateLimiter(properties.capacity(), properties.refillPerSec(), System::nanoTime);
    }
}
