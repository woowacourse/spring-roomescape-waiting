package roomescape.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 들어오는 요청에 토큰 버킷 Rate Limit 을 적용하는 인터셉터.
 *
 * <p>토큰이 없으면 컨트롤러를 호출하지 않고 429 + Retry-After 로 거부한다. 결제로 이어지는 요청(쓰기 요청과
 * 결제 승인 트리거)만 토큰을 소비하므로, 같은 경로의 조회(GET)는 한도와 무관하게 통과한다.
 */
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final String PAYMENT_CONFIRM_PATH = "/payments/success";

    private final TokenBucketRateLimiter rateLimiter;

    public RateLimitInterceptor(TokenBucketRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!consumesToken(request)) {
            return true;
        }
        if (rateLimiter.tryConsume()) {
            return true;
        }
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(rateLimiter.retryAfterSeconds()));
        return false;
    }

    private boolean consumesToken(HttpServletRequest request) {
        return HttpMethod.POST.matches(request.getMethod())
                || PAYMENT_CONFIRM_PATH.equals(request.getRequestURI());
    }
}
