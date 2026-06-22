package roomescape.ratelimit;

import java.util.function.LongSupplier;
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
    public LongSupplier rateLimitNanoTime() {
        return System::nanoTime;
    }

    @Bean
    public TokenBucketRateLimiter inboundRateLimiter(RateLimitProperties properties, LongSupplier rateLimitNanoTime) {
        return new TokenBucketRateLimiter(properties.capacity(), properties.refillPerSecond(), rateLimitNanoTime);
    }

    @Bean
    public TokenBucketRateLimiter outboundRateLimiter(OutboundRateLimitProperties properties,
                                                      LongSupplier rateLimitNanoTime) {
        return new TokenBucketRateLimiter(properties.capacity(), properties.refillPerSecond(), rateLimitNanoTime);
    }

    @Bean
    public RateLimitInterceptor rateLimitInterceptor(@Qualifier("inboundRateLimiter")
                                                     TokenBucketRateLimiter inboundRateLimiter) {
        return new RateLimitInterceptor(inboundRateLimiter);
    }

    @Bean
    public OutboundRateLimitInterceptor outboundRateLimitInterceptor(@Qualifier("outboundRateLimiter")
                                                                    TokenBucketRateLimiter outboundRateLimiter) {
        return new OutboundRateLimitInterceptor(outboundRateLimiter);
    }

    @Bean
    public RetrySleeper retrySleeper() {
        return duration -> Thread.sleep(duration.toMillis());
    }

    @Bean
    public RetryAfterInterceptor retryAfterInterceptor(OutboundRateLimitProperties properties,
                                                       RetrySleeper retrySleeper) {
        return new RetryAfterInterceptor(properties.maxAttempts(), properties.fallbackDelay(), retrySleeper);
    }

    @Bean
    public WebMvcConfigurer rateLimitWebMvcConfigurer(RateLimitInterceptor rateLimitInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(rateLimitInterceptor)
                        .addPathPatterns("/reservations/**", "/reservation-waitings/**", "/payments/**");
            }
        };
    }
}
