package roomescape.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.payment.ratelimit.RateLimitInterceptor;
import roomescape.payment.ratelimit.TokenBucketRateLimiter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final long capacity;
    private final double refillPerSecond;

    public WebConfig(
            @Value("${rate-limit.capacity}") long capacity,
            @Value("${rate-limit.refill-per-second}") double refillPerSecond
    ) {
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
    }

    @Bean
    public TokenBucketRateLimiter inboundRateLimiter() {
        return new TokenBucketRateLimiter(capacity, refillPerSecond, System::nanoTime);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(inboundRateLimiter()))
                .addPathPatterns("/payments/**", "/reservations/**");
    }
}
