package roomescape.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

public class RateLimitInterceptor implements HandlerInterceptor {

    private static final String ERROR_RESPONSE = "{\"message\":\"요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.\"}";

    private final TokenBucketRateLimiter rateLimiter;

    public RateLimitInterceptor(TokenBucketRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (rateLimiter.tryConsume()) {
            return true;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(rateLimiter.retryAfterSeconds()));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(ERROR_RESPONSE);
        return false;
    }
}
