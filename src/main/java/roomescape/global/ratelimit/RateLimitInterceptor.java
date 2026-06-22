package roomescape.global.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final TokenBucketRateLimiter inboundRateLimiter;

    public RateLimitInterceptor(@Qualifier("inboundRateLimiter") TokenBucketRateLimiter inboundRateLimiter) {
        this.inboundRateLimiter = inboundRateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!inboundRateLimiter.tryConsume()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
            response.setHeader("Retry-After", String.valueOf(inboundRateLimiter.retryAfterSeconds()));
            response.getWriter().write("{\"message\":\"요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.\"}");
            return false;
        }
        return true;
    }
}
