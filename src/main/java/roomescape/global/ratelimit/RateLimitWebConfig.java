package roomescape.global.ratelimit;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RateLimitWebConfig implements WebMvcConfigurer {
    private final TokenBucketRateLimiter rateLimiter;

    public RateLimitWebConfig(RateLimitProperties properties) {
        this.rateLimiter = new TokenBucketRateLimiter(
                properties.capacity(),
                properties.refillPerSec(),
                System::nanoTime
        );
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(rateLimiter))
                .addPathPatterns("/payments/**", "/reservations/**");
    }
}
