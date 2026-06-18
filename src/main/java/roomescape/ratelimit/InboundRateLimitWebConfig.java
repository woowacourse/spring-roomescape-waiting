package roomescape.ratelimit;

import java.util.function.LongSupplier;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InboundRateLimitWebConfig implements WebMvcConfigurer {

    private final RateLimitProperties properties;
    private final LongSupplier nanoTime;

    public InboundRateLimitWebConfig(RateLimitProperties properties, LongSupplier nanoTime) {
        this.properties = properties;
        this.nanoTime = nanoTime;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (!properties.enabled()) {
            return;
        }
        TokenBucket rateLimiter = new TokenBucket(properties.capacity(), properties.refillPerSecond(), nanoTime);
        registry.addInterceptor(new InboundRateLimitInterceptor(rateLimiter))
                .addPathPatterns(properties.pathPatterns());
    }
}
