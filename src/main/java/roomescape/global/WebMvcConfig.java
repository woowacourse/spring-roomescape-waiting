package roomescape.global;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.payment.ratelimit.RateLimitInterceptor;
import roomescape.payment.ratelimit.TokenBucketRateLimiter;

/**
 * Rate Limit 인터셉터를 결제·예약 경로에 등록한다. 한도 정책은 rate-limit.* 프로퍼티로 외부화한다.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final TokenBucketRateLimiter rateLimiter;

    public WebMvcConfig(
            @Value("${rate-limit.capacity}") long capacity,
            @Value("${rate-limit.refill-per-second}") double refillPerSecond
    ) {
        this.rateLimiter = new TokenBucketRateLimiter(capacity, refillPerSecond, System::nanoTime);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(rateLimiter))
                .addPathPatterns("/payment", "/payment/**", "/payments/**", "/reservations", "/reservations/**");
    }
}
