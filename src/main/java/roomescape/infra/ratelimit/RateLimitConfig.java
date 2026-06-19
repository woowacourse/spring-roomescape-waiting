package roomescape.infra.ratelimit;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig implements WebMvcConfigurer {

    private final RateLimitProperties properties;

    public RateLimitConfig(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Bean
    public TokenBucket rateLimitTokenBucket() {
        return TokenBucket.ofRealTime(properties.capacity(), properties.refillPerSec());
    }

    @Bean
    public RateLimitInterceptor rateLimitInterceptor(TokenBucket rateLimitTokenBucket) {
        return new RateLimitInterceptor(rateLimitTokenBucket);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor(rateLimitTokenBucket()))
                .addPathPatterns("/reservations/**", "/payments/**");
    }
}