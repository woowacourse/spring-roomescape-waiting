package roomescape.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 들어오는 Rate Limit 인터셉터를 결제·예약 핵심 경로에 등록한다. 한도 정책은 {@code rate-limit.*} 로 외부화한다.
 */
@Configuration
public class RateLimitWebConfig implements WebMvcConfigurer {

    private final TokenBucketRateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    public RateLimitWebConfig(
            ObjectMapper objectMapper,
            @Value("${rate-limit.capacity:100}") long capacity,
            @Value("${rate-limit.refill-per-second:100}") double refillPerSecond
    ) {
        this.objectMapper = objectMapper;
        this.rateLimiter = new TokenBucketRateLimiter(capacity, refillPerSecond, System::nanoTime);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(rateLimiter, objectMapper))
                .addPathPatterns("/reservations", "/reservations/*/payments", "/payments/success");
    }
}
