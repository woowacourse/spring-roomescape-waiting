package roomescape.payment.client;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.payment.exception.OutboundRateLimitException;
import roomescape.ratelimit.TokenBucketRateLimiter;

// 나가는(Outbound) 호출에 거는 처리율 제한기 인터셉터. 외부 호출 '전에' 토큰을 소비해, 한도를 넘으면 보내지 않고 예외로 거부.
public class OutboundRateLimitInterceptor implements ClientHttpRequestInterceptor {

  private final TokenBucketRateLimiter rateLimiter;

  public OutboundRateLimitInterceptor(TokenBucketRateLimiter rateLimiter) {
    this.rateLimiter = rateLimiter;
  }

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body,
      ClientHttpRequestExecution execution) throws IOException {
    if (!rateLimiter.tryConsume()) {
      throw new OutboundRateLimitException("나가는 호출이 자체 Rate Limit을 초과해 외부로 보내지 않았습니다.");
    }
    return execution.execute(request, body);
  }
}
