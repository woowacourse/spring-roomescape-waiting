package roomescape.payment.presentation.ratelimit.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.payment.presentation.ratelimit.policy.TokenBucketRateLimiter;

/**
 * 들어오는 요청에 거는 Rate Limit 인터셉터. 토큰이 없으면 컨트롤러를 호출하지 않고 429 + Retry-After 로 거부한다.
 */
public class RateLimitInterceptor implements HandlerInterceptor {

  private final TokenBucketRateLimiter rateLimiter;

  public RateLimitInterceptor(TokenBucketRateLimiter rateLimiter) {
    this.rateLimiter = rateLimiter;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    // TODO: tryConsume() 이 false 면 429 상태와 Retry-After 헤더(retryAfterSeconds())를 세팅하고 false 를 반환한다.
    // 지금은 항상 통과시키므로 한도 초과 테스트가 실패한다.
    if (!rateLimiter.tryConsume()) {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(rateLimiter.retryAfterSeconds()));
      return false;
    }
    return true;
  }

}
