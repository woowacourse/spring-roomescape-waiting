package roomescape.infra.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

public class RateLimitInterceptor implements HandlerInterceptor {

    private final TokenBucket tokenBucket;

    public RateLimitInterceptor(TokenBucket tokenBucket) {
        this.tokenBucket = tokenBucket;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (tokenBucket.tryConsume()) {
            return true;
        }
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(tokenBucket.retryAfterSeconds()));
        return false;
    }
}