package roomescape.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    public RateLimitConfig(
            @Value("${rate-limit.capacity}") long capacity,
            @Value("${rate-limit.refill-per-sec}") double refillPerSec
    ) {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(capacity, refillPerSec, System::nanoTime);
        this.rateLimitInterceptor = new RateLimitInterceptor(rateLimiter);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns(
                        "/payments/**",
                        "/user/reservations/**",
                        "/admin/reservations/**",
                        "/user/waitings/**"
                );
    }
}
