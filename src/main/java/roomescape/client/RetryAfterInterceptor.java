package roomescape.client;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * 토스가 429 를 주면 Retry-After(초)만큼 기다렸다 재시도하는 인터셉터. 같은 HttpRequest(멱등키 헤더 포함)를 그대로 재전송한다.
 * maxAttempts 까지 시도해도 429면 무한 재시도하지 않고 {@link TossRateLimitExceededException} 으로 실패시킨다.
 */
public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {

    private static final long DEFAULT_RETRY_AFTER_SECONDS = 1L;

    private final int maxAttempts;

    public RetryAfterInterceptor(int maxAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts(최초 시도를 포함한 총 시도 횟수)는 1 이상이어야 합니다: " + maxAttempts);
        }
        this.maxAttempts = maxAttempts;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        var response = execution.execute(request, body);
        var attempt = 1;
        while (isTooManyRequests(response) && attempt < maxAttempts) {
            var waitSeconds = parseRetryAfterSeconds(response);
            response.close();
            sleepSeconds(waitSeconds);
            response = execution.execute(request, body);
            attempt++;
        }
        if (isTooManyRequests(response)) {
            response.close();
            throw new TossRateLimitExceededException();
        }
        return response;
    }

    private boolean isTooManyRequests(ClientHttpResponse response) throws IOException {
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
