package roomescape.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Predicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

public class RateLimitInterceptor implements HandlerInterceptor {

    private final TokenBucketRateLimiter rateLimiter;
    private final Predicate<HttpServletRequest> shouldLimit;

    public RateLimitInterceptor(TokenBucketRateLimiter rateLimiter) {
        this(rateLimiter, request -> true);
    }

    public RateLimitInterceptor(TokenBucketRateLimiter rateLimiter, Predicate<HttpServletRequest> shouldLimit) {
        this.rateLimiter = rateLimiter;
        this.shouldLimit = shouldLimit;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!shouldLimit.test(request) || rateLimiter.tryConsume()) {
            return true;
        }
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(rateLimiter.retryAfterSeconds()));
        return false;
    }
}
