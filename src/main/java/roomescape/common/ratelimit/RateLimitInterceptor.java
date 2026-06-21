package roomescape.common.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 들어오는 요청 Rate Limit(서버 관점). 결제·예약 엔드포인트 앞단(preHandle)에서 토큰을 소비하고,
 * 없으면 컨트롤러를 호출하지 않고(false 반환) 429 + Retry-After(초)로 거부한다.
 */
public class RateLimitInterceptor implements HandlerInterceptor {

    private final TokenBucketRateLimiter rateLimiter;

    public RateLimitInterceptor(TokenBucketRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (rateLimiter.tryConsume()) {
            return true;
        }
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(rateLimiter.retryAfterSeconds()));
        return false;
    }
}
