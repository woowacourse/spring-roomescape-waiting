package roomescape.infrastructure.ratelimiter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    @Bean
    public TokenBucket tokenBucket(RateLimitProperties properties) {
        return new TokenBucket(
                properties.getCapacity(),
                properties.getRefillPerSec(),
                System::nanoTime
        );
    }
}
