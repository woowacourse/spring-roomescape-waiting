package roomescape.payment.infra.client.interceptor;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * 게이트웨이가 429 를 주면 Retry-After(초)만큼 기다렸다 재시도하는 인터셉터. maxAttempts 회까지 시도한다.
 */
public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {

  private static final long DEFAULT_RETRY_AFTER_SECONDS = 1L;

  private final int maxAttempts;

  public RetryAfterInterceptor(int maxAttempts) {
    this.maxAttempts = maxAttempts;
  }

  @Override
  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    // TODO: 응답이 429 이고 시도 횟수가 maxAttempts 미만이면, 이전 응답을 close() 하고 Retry-After 만큼 대기 후 재시도한다.
    // 지금은 재시도 없이 첫 응답을 그대로 반환하므로, 429 를 받으면 그대로 실패한다.
    ClientHttpResponse response = execution.execute(request, body);
    int attempt = 1;
    while (isTooManyRequest(response) && attempt < maxAttempts) {
      long waitSecond = parseRetryAfterSeconds(response);
      response.close();
      sleepSeconds(waitSecond);
      response = execution.execute(request, body);
      attempt++;
    }
    return response;
  }

  private boolean isTooManyRequest(ClientHttpResponse response) throws IOException {
    return response.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value();
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
