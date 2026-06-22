package roomescape.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final TokenBucketRateLimiter rateLimiter;

    public RateLimitInterceptor(TokenBucketRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (rateLimiter.tryConsume()) {
            return true;
        }

        long retryAfterSeconds = rateLimiter.retryAfterSeconds();
        log.warn("Rate limit 초과 — 요청 거부: method={}, uri={}, retryAfter={}s",
                request.getMethod(), request.getRequestURI(), retryAfterSeconds);

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
                "{\"exceptionCode\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"요청 한도를 초과했습니다. %d초 후 다시 시도해 주세요.\"}",
                retryAfterSeconds
        ));
        return false;
    }
}
