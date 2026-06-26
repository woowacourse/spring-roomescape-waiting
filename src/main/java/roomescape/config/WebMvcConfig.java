package roomescape.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.ratelimit.OutboundRateLimitProperties;
import roomescape.ratelimit.RateLimitInterceptor;
import roomescape.ratelimit.RateLimitProperties;
import roomescape.ratelimit.TokenBucketRateLimiter;

@Configuration
@EnableConfigurationProperties({RateLimitProperties.class, OutboundRateLimitProperties.class})
public class WebMvcConfig implements WebMvcConfigurer {
    private final RateLimitProperties rateLimitProperties;

    public WebMvcConfig(RateLimitProperties rateLimitProperties) {
        this.rateLimitProperties = rateLimitProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(
                rateLimitProperties.capacity(), rateLimitProperties.refillPerSec(), System::nanoTime
        );
        registry.addInterceptor(new RateLimitInterceptor(limiter))
                .addPathPatterns("/reservations/**", "/payments/**");
    }
}
