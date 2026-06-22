package roomescape.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Rate Limit 인터셉터를 결제·예약 경로에 등록한다. 한도 정책(capacity/refillPerSec)은
 * rate-limit.* 프로퍼티로 외부화해, 코드 수정 없이 거부 시점을 바꾼다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TokenBucketRateLimiter rateLimiter;

    public WebConfig(
            @Value("${rate-limit.capacity}") long capacity,
            @Value("${rate-limit.refill-per-second}") double refillPerSecond
    ) {
        this.rateLimiter = new TokenBucketRateLimiter(capacity, refillPerSecond, System::nanoTime);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(rateLimiter))
                .addPathPatterns("/payments/**", "/user/reservations", "/user/reservations/**");
    }
}
