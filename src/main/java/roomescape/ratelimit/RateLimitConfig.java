package roomescape.ratelimit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        RateLimitProperties.class,
        OutboundRateLimitProperties.class
})
public class RateLimitConfig {

    @Bean("inboundRateLimiter")
    TokenBucketRateLimiter inboundRateLimiter(RateLimitProperties properties) {
        return new TokenBucketRateLimiter(properties.capacity(), properties.refillPerSec());
    }

    @Bean("outboundRateLimiter")
    TokenBucketRateLimiter outboundRateLimiter(OutboundRateLimitProperties properties) {
        return new TokenBucketRateLimiter(properties.capacity(), properties.refillPerSec());
    }

    @Bean
    RateLimitInterceptor rateLimitInterceptor(
            @Qualifier("inboundRateLimiter") TokenBucketRateLimiter rateLimiter
    ) {
        return new RateLimitInterceptor(rateLimiter);
    }
}
