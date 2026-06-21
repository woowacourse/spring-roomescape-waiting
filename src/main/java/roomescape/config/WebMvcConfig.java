package roomescape.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.ratelimit.RateLimitInterceptor;
import roomescape.ratelimit.TokenBucketRateLimiter;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitProperties properties;

    public WebMvcConfig(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(
                properties.getCapacity(),
                properties.getRefillPerSec(),
                System::nanoTime
        );
        registry.addInterceptor(new RateLimitInterceptor(rateLimiter))
                .addPathPatterns("/payments/**", "/payment/**", "/reservations/**");
    }
}