package roomescape.payment.infra.client.interceptor;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.payment.infra.client.exception.OutboundRateLimitException;
import roomescape.payment.presentation.ratelimit.policy.TokenBucketRateLimiter;

/**
 * 나가는(outbound) 호출에 거는 Rate Limit 인터셉터. 외부 호출 '전에' 토큰을 소비해, 한도를 넘으면 보내지 않고 {@link OutboundRateLimitException} 으로 거부한다.
 */
public class OutboundRateLimitInterceptor implements ClientHttpRequestInterceptor {

  private final TokenBucketRateLimiter rateLimiter;

  public OutboundRateLimitInterceptor(TokenBucketRateLimiter rateLimiter) {
    this.rateLimiter = rateLimiter;
  }

  @Override
  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    // TODO: tryConsume() 이 false 면 execution 을 호출하지 말고 OutboundRateLimitException 을 던진다.
    // 지금은 한도와 무관하게 그대로 내보내므로, 한도 초과 거부 테스트가 실패한다.
    if (!rateLimiter.tryConsume()) {
      throw new OutboundRateLimitException("나가는 호출이 자체 Rate Limit을 초과해 외부로 보내지 않았습니다.");
    }
    return execution.execute(request, body);
  }

}
