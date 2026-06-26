package roomescape.payment.client;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.payment.exception.TossPaymentException;

public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {

  private static final long DEFAULT_RETRY_AFTER_SECONDS = 1L;

  private final int maxAttempts;

  public RetryAfterInterceptor(int maxAttempts) {
    this.maxAttempts = maxAttempts;
  }

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body,
      ClientHttpRequestExecution execution) throws IOException {
    var response = execution.execute(request, body);
    var attempt = 1;
    while (response.getStatusCode().value() == 429 && attempt < maxAttempts) {
      var waitSeconds = parseRetryAfterSeconds(response);
      response.close();
      sleepSeconds(waitSeconds);
      response = execution.execute(request, body);
      attempt++;
    }

    if (response.getStatusCode().value() == 429) {
      response.close();
      throw new TossPaymentException.RateLimitExceeded("결제사 요청 한도 초과: 재시도 횟수 소진");
    }

    return response;
  }

  private long parseRetryAfterSeconds(ClientHttpResponse response) {
    var value = response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER);
    if (value == null) {
      return DEFAULT_RETRY_AFTER_SECONDS;
    }
    try {
      return Math.max(0, Long.parseLong(value.trim()));
    } catch (NumberFormatException e) {
      return DEFAULT_RETRY_AFTER_SECONDS;
    }
  }

  private void sleepSeconds(long seconds) {
    try {
      Thread.sleep(seconds * 1000L);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("재시도 대기 중 인터럽트되었습니다.", e);
    }
  }

}
