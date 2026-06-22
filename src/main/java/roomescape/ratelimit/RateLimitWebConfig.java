package roomescape.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RateLimitWebConfig implements WebMvcConfigurer {

    private final TokenBucketRateLimiter rateLimiter;

    public RateLimitWebConfig(
            @Value("${rate-limit.capacity}") long capacity,
            @Value("${rate-limit.refill-per-second}") double refillPerSecond
    ) {
        this.rateLimiter = new TokenBucketRateLimiter(capacity, refillPerSecond, System::nanoTime);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(rateLimiter, this::isProtectedEndpoint))
                .addPathPatterns("/payments/**", "/reservations", "/reservations/waitings");
    }

    private boolean isProtectedEndpoint(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        if (path.startsWith("/payments/")) {
            return true;
        }
        return HttpMethod.POST.matches(method)
                && ("/reservations".equals(path) || "/reservations/waitings".equals(path));
    }
}
