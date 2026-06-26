package roomescape.infrastructure.toss;

import java.io.IOException;
import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {
    private final int maxAttempts;
    private final Duration fallbackBackOff;
    private final BackOffSleeper sleeper;

    public RetryAfterInterceptor(int maxAttempts, Duration fallbackBackOff, BackOffSleeper sleeper) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be positive");
        }
        this.maxAttempts = maxAttempts;
        this.fallbackBackOff = fallbackBackOff;
        this.sleeper = sleeper;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            ClientHttpResponse response = execution.execute(request, body);
            if (!HttpStatus.TOO_MANY_REQUESTS.equals(response.getStatusCode())) {
                return response;
            }
            if (attempt == maxAttempts) {
                response.close();
                throw new TossPaymentException.RateLimited("토스 결제 승인 요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.");
            }
            Duration backOff = retryAfter(response.getHeaders());
            response.close();
            sleep(backOff);
        }
        throw new TossPaymentException.RateLimited("토스 결제 승인 요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.");
    }

    private Duration retryAfter(HttpHeaders headers) {
        String retryAfter = headers.getFirst(HttpHeaders.RETRY_AFTER);
        if (retryAfter == null) {
            return fallbackBackOff;
        }
        try {
            long seconds = Long.parseLong(retryAfter);
            return Duration.ofSeconds(Math.max(0, seconds));
        } catch (NumberFormatException e) {
            return fallbackBackOff;
        }
    }

    private void sleep(Duration duration) throws IOException {
        try {
            sleeper.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted during Retry-After backoff", e);
        }
    }
}
