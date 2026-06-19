package roomescape.infrastructure.ratelimit;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.exception.TossPaymentException;

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
        var response = execution.execute(request, body);
        var attempt = 1;
        while (isTooManyRequests(response) && attempt < maxAttempts) {
            var waitSeconds = parseRetryAfterSeconds(response);
            response.close();           // 재시도 전 이전 응답 자원 해제
            sleepSeconds(waitSeconds);
            response = execution.execute(request, body);
            attempt++;
        }

        if (isTooManyRequests(response)) {
            throw new TossPaymentException(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS", "최대 재시도 횟수를 초과했습니다.");
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
