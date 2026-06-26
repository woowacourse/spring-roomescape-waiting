package roomescape.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    @Bean
    public TokenBucketRateLimiter inboundRateLimiter(
            @Value("${rate-limit.capacity}") long capacity,
            @Value("${rate-limit.refill-per-sec}") double refillPerSec
    ) {
        return new TokenBucketRateLimiter(capacity, refillPerSec, System::nanoTime);
    }
}
