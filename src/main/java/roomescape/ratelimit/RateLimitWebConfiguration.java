package roomescape.ratelimit;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RateLimitWebConfiguration implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    public RateLimitWebConfiguration(RateLimitInterceptor rateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
            .addPathPatterns(
                "/payments/**",
                "/confirm/payment",
                "/reservations/**",
                "/reservation/**"
            );
    }
}
