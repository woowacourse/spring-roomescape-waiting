package roomescape.reservation.infra.toss;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Toss가 429를 응답하면 Retry-After(초)만큼 기다렸다 같은 요청을 재시도하는 인터셉터.
 */
public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {

    private static final long DEFAULT_RETRY_AFTER_SECONDS = 1L;

    private final int maxAttempts;

    public RetryAfterInterceptor(int maxAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("최대 시도 횟수는 1 이상이어야 합니다.");
        }
        this.maxAttempts = maxAttempts;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        var response = execution.execute(request, body);

        int attempt = 1;
        while (isTooManyRequests(response) && attempt < maxAttempts) {
            long waitSeconds = parseRetryAfterSeconds(response);
            response.close();
            sleepSeconds(waitSeconds);
            response = execution.execute(request, body);
            attempt++;
        }

        return response;
    }

    private boolean isTooManyRequests(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value();
    }

    private long parseRetryAfterSeconds(ClientHttpResponse response) {
        var retryAfter = response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER);
        if (retryAfter == null) {
            return DEFAULT_RETRY_AFTER_SECONDS;
        }

        try {
            return Math.max(0, Long.parseLong(retryAfter.trim()));
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
