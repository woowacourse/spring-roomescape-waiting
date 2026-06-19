package roomescape.ratelimit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class RateLimitConfig {

    @Bean
    public TokenBucketRateLimiter inboundRateLimiter(RateLimitProperties properties) {
        return new TokenBucketRateLimiter(properties.getCapacity(), properties.getRefillPerSecond());
    }

    @Bean
    public RateLimitInterceptor rateLimitInterceptor(@Qualifier("inboundRateLimiter") TokenBucketRateLimiter inboundRateLimiter) {
        return new RateLimitInterceptor(inboundRateLimiter);
    }

    @Bean
    public TokenBucketRateLimiter outboundRateLimiter(OutboundRateLimitProperties properties) {
        return new TokenBucketRateLimiter(properties.getCapacity(), properties.getRefillPerSecond());
    }

    @Bean
    public OutboundRateLimitInterceptor outboundRateLimitInterceptor(@Qualifier("outboundRateLimiter") TokenBucketRateLimiter outboundRateLimiter) {
        return new OutboundRateLimitInterceptor(outboundRateLimiter);
    }

    @Bean
    public RetryAfterInterceptor retryAfterInterceptor(RetryAfterProperties properties) {
        return new RetryAfterInterceptor(properties);
    }
}
