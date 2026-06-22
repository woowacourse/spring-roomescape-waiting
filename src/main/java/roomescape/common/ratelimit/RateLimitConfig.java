package roomescape.common.ratelimit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {

    @Bean
    public RateLimitInterceptor rateLimitInterceptor(
            @Qualifier("inboundRateLimiter") final TokenBucketRateLimiter rateLimiter
    ) {
        return new RateLimitInterceptor(rateLimiter);
    }

    @Bean(name = "inboundRateLimiter")
    public TokenBucketRateLimiter inboundRateLimiter(final RateLimitProperties properties) {
        return new TokenBucketRateLimiter(
                properties.capacity(),
                properties.refillPerSec(),
                System::nanoTime
        );
    }
}
