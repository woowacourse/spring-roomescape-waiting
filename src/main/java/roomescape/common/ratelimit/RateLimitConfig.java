package roomescape.common.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    private final TokenBucketRateLimiter inboundRateLimiter;

    public RateLimitConfig(
            @Value("${rate-limit.capacity}") long capacity,
            @Value("${rate-limit.refill-per-sec}") double refillPerSec
    ) {
        this.inboundRateLimiter = new TokenBucketRateLimiter(capacity, refillPerSec, System::nanoTime);
    }

    @Bean
    public TokenBucketRateLimiter inboundRateLimiter() {
        return inboundRateLimiter;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(inboundRateLimiter))
                .addPathPatterns(
                        "/api/v1/payments/**",
                        "/api/v1/reservations",
                        "/api/v1/reservations/**"
                );
    }
}
