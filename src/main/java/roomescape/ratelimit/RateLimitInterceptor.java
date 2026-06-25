package roomescape.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final TokenBucketRateLimiter rateLimiter;

    @Autowired
    public RateLimitInterceptor(
        @Value("${rate-limit.capacity}") long capacity,
        @Value("${rate-limit.refill-per-sec}") double refillPerSecond
    ) {
        this(new TokenBucketRateLimiter(capacity, refillPerSecond));
    }

    RateLimitInterceptor(TokenBucketRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) {
        if (rateLimiter.tryConsume()) {
            return true;
        }
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(
            HttpHeaders.RETRY_AFTER,
            Long.toString(rateLimiter.retryAfterSeconds())
        );
        return false;
    }
}
