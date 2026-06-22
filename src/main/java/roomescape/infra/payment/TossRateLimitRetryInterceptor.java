package roomescape.infra.payment;

import java.io.IOException;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClientException;
import roomescape.exception.server.PaymentRateLimitExceededException;

public class TossRateLimitRetryInterceptor implements ClientHttpRequestInterceptor {

    private final int maxAttempts;
    private final Duration fallbackRetryAfter;
    private final RetrySleeper retrySleeper;

    public TossRateLimitRetryInterceptor(int maxAttempts, Duration fallbackRetryAfter) {
        this(maxAttempts, fallbackRetryAfter, Thread::sleep);
    }

    TossRateLimitRetryInterceptor(int maxAttempts, Duration fallbackRetryAfter, RetrySleeper retrySleeper) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts는 0보다 커야 합니다.");
        }
        if (fallbackRetryAfter.isNegative()) {
            throw new IllegalArgumentException("fallbackRetryAfter는 음수일 수 없습니다.");
        }
        this.maxAttempts = maxAttempts;
        this.fallbackRetryAfter = fallbackRetryAfter;
        this.retrySleeper = retrySleeper;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        int attempt = 1;

        while (true) {
            ClientHttpResponse response = execution.execute(request, body);
            if (!isTooManyRequests(response)) {
                return response;
            }

            if (attempt >= maxAttempts) {
                response.close();
                throw new PaymentRateLimitExceededException();
            }

            Duration retryAfter = retryAfter(response);
            response.close();
            sleep(retryAfter);
            attempt++;
        }
    }

    private boolean isTooManyRequests(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS);
    }

    private Duration retryAfter(ClientHttpResponse response) {
        String retryAfter = response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER);
        if (retryAfter == null || retryAfter.isBlank()) {
            return fallbackRetryAfter;
        }

        try {
            long seconds = Long.parseLong(retryAfter);
            if (seconds < 0) {
                return fallbackRetryAfter;
            }
            return Duration.ofSeconds(seconds);
        } catch (NumberFormatException e) {
            return fallbackRetryAfter;
        }
    }

    private void sleep(Duration retryAfter) {
        try {
            retrySleeper.sleep(retryAfter);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RestClientException("결제 승인 재시도 대기 중 인터럽트가 발생했습니다.", e);
        }
    }

    @FunctionalInterface
    interface RetrySleeper {

        void sleep(Duration duration) throws InterruptedException;
    }
}
