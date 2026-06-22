package roomescape.infrastructure;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.infrastructure.ratelimit.RateLimitInterceptor;
import roomescape.infrastructure.ratelimit.TokenBucketRateLimiter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final LoginUserArgumentResolver loginUserArgumentResolver;
    private final TokenBucketRateLimiter rateLimiter;

    public WebConfig(
            AuthInterceptor authInterceptor,
            LoginUserArgumentResolver loginUserArgumentResolver,
            @Value("${rate-limit.capacity}") long capacity,
            @Value("${rate-limit.refill-per-second}") double refillPerSecond
    ) {
        this.authInterceptor = authInterceptor;
        this.loginUserArgumentResolver = loginUserArgumentResolver;
        this.rateLimiter = new TokenBucketRateLimiter(capacity, refillPerSecond, System::nanoTime);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor);
        registry.addInterceptor(new RateLimitInterceptor(rateLimiter))
                .addPathPatterns("/payments/**", "/reservations/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginUserArgumentResolver);
    }
}
