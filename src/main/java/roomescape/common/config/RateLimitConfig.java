package roomescape.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.common.ratelimit.RateLimitInterceptor;
import roomescape.common.ratelimit.TokenBucketRateLimiter;

/**
 * 같은 토큰 버킷 알고리즘을 방향만 다르게 두 곳(들어오는/나가는)에 적용한다.
 * 들어오는 한도는 결제·예약 엔드포인트에 인터셉터로 건다.
 */
@Configuration
@EnableConfigurationProperties({RateLimitProperties.class, OutboundRateLimitProperties.class})
public class RateLimitConfig implements WebMvcConfigurer {

    private final RateLimitProperties inboundProperties;
    private final OutboundRateLimitProperties outboundProperties;

    public RateLimitConfig(RateLimitProperties inboundProperties, OutboundRateLimitProperties outboundProperties) {
        this.inboundProperties = inboundProperties;
        this.outboundProperties = outboundProperties;
    }

    @Bean
    public TokenBucketRateLimiter inboundRateLimiter() {
        return new TokenBucketRateLimiter(
                inboundProperties.capacity(), inboundProperties.refillPerSec(), System::nanoTime);
    }

    @Bean
    public TokenBucketRateLimiter outboundRateLimiter() {
        return new TokenBucketRateLimiter(
                outboundProperties.capacity(), outboundProperties.refillPerSec(), System::nanoTime);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(inboundRateLimiter()))
                .addPathPatterns("/api/payments/**", "/api/reservations/**");
    }
}
