package roomescape.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.MappedInterceptor;
import roomescape.ratelimit.RateLimitInterceptor;
import roomescape.ratelimit.TokenBucketRateLimiter;

@Configuration
public class RateLimitConfig {

    @Bean
    public MappedInterceptor rateLimitInterceptor(
            @Value("${rate-limit.capacity}") long capacity,
            @Value("${rate-limit.refill-per-sec}") double refillPerSec
    ) {
        TokenBucketRateLimiter inboundRateLimiter = new TokenBucketRateLimiter(capacity, refillPerSec, System::nanoTime);
        return new MappedInterceptor(
                new String[]{"/reservations", "/reservations/**", "/payments", "/payments/**"},
                new RateLimitInterceptor(inboundRateLimiter));
    }
}
