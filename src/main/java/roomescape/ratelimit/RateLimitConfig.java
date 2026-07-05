package roomescape.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RateLimitConfig implements WebMvcConfigurer {
    private final long capacity;
    private final double refillPerSecond;

    public RateLimitConfig(
            @Value("${rate-limit.capacity}") long capacity,
            @Value("${rate-limit.refill-per-sec}") double refillPerSecond
    ) {
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
    }

    @Bean
    public TokenBucketRateLimiter inboundRateLimiter() {
        return new TokenBucketRateLimiter(capacity, refillPerSecond, System::nanoTime);
    }

    @Bean
    public RateLimitInterceptor rateLimitInterceptor(
            @Qualifier("inboundRateLimiter") TokenBucketRateLimiter inboundRateLimiter
    ) {
        return new RateLimitInterceptor(inboundRateLimiter);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor(inboundRateLimiter()))
                .addPathPatterns("/payments/**", "/reservations/**");
    }
}
