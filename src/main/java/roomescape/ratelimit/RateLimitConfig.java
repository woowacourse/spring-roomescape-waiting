package roomescape.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties({RateLimitProperties.class, OutboundRateLimitProperties.class})
public class RateLimitConfig {

    @Bean
    public TokenBucketRateLimiter inboundRateLimiter(RateLimitProperties properties) {
        return new TokenBucketRateLimiter(properties.capacity(), properties.refillPerSecond());
    }

    @Bean
    public TokenBucketRateLimiter outboundRateLimiter(OutboundRateLimitProperties properties) {
        return new TokenBucketRateLimiter(properties.capacity(), properties.refillPerSecond());
    }

    @Bean
    public RateLimitInterceptor rateLimitInterceptor(
            @Qualifier("inboundRateLimiter") TokenBucketRateLimiter rateLimiter,
            ObjectMapper objectMapper
    ) {
        return new RateLimitInterceptor(rateLimiter, objectMapper);
    }

    @Bean
    public OutboundRateLimitInterceptor outboundRateLimitInterceptor(
            @Qualifier("outboundRateLimiter") TokenBucketRateLimiter rateLimiter
    ) {
        return new OutboundRateLimitInterceptor(rateLimiter);
    }

    @Bean
    public RetryAfterInterceptor retryAfterInterceptor(OutboundRateLimitProperties properties, Sleeper sleeper) {
        return new RetryAfterInterceptor(
                properties.maxAttempts(),
                properties.fallbackRetryAfter(),
                sleeper
        );
    }

    @Bean
    public Sleeper sleeper() {
        return duration -> Thread.sleep(duration.toMillis());
    }

    @Bean
    public WebMvcConfigurer rateLimitWebMvcConfigurer(RateLimitInterceptor rateLimitInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(rateLimitInterceptor)
                        .addPathPatterns(
                                "/api/user/reservations/**",
                                "/api/user/payments/**"
                        );
            }
        };
    }
}
