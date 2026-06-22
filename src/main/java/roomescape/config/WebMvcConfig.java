package roomescape.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.ratelimit.RateLimitInterceptor;
import roomescape.ratelimit.RateLimitProperties;
import roomescape.ratelimit.TokenBucketRateLimiter;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class WebMvcConfig implements WebMvcConfigurer {

    private final TokenBucketRateLimiter rateLimiter;

    public WebMvcConfig(RateLimitProperties properties) {
        this.rateLimiter = new TokenBucketRateLimiter(
                properties.capacity(), properties.refillPerSecond(), System::nanoTime);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(rateLimiter))
                .addPathPatterns("/user/**");
    }
}
