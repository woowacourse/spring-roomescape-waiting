package roomescape.global.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 예약/대기/결제 요청에 Rate Limit 인터셉터를 등록합니다.
 */
@Configuration
public class RateLimitWebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    public RateLimitWebConfig(
            @Value("${rate-limit.capacity}") long capacity,
            @Value("${rate-limit.refill-per-second}") double refillPerSecond
    ) {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(capacity, refillPerSecond, System::nanoTime);
        this.rateLimitInterceptor = new RateLimitInterceptor(rateLimiter);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/payments/**", "/reservations/**", "/waitings/**");
    }
}
