package roomescape.ratelimit;

import java.io.IOException;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.infrastructure.payment.toss.toss.TossPaymentException;

public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {

    private final int maxAttempts;
    private final Duration fallbackDelay;
    private final RetrySleeper retrySleeper;

    public RetryAfterInterceptor(int maxAttempts, Duration fallbackDelay, RetrySleeper retrySleeper) {
        this.maxAttempts = maxAttempts;
        this.fallbackDelay = fallbackDelay;
        this.retrySleeper = retrySleeper;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        int attempt = 1;
        while (true) {
            ClientHttpResponse response = execution.execute(request, body);
            if (!response.getStatusCode().isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS)) {
                return response;
            }
            if (attempt >= maxAttempts) {
                response.close();
                throw new TossPaymentException.RateLimited("토스 결제 승인 호출 한도를 초과했습니다.");
            }
            Duration retryDelay = retryDelay(response.getHeaders());
            response.close();
            sleep(retryDelay);
            attempt++;
        }
    }

    private Duration retryDelay(HttpHeaders headers) {
        String retryAfter = headers.getFirst(HttpHeaders.RETRY_AFTER);
        if (retryAfter == null) {
            return fallbackDelay;
        }
        try {
            long seconds = Long.parseLong(retryAfter);
            return Duration.ofSeconds(Math.max(0L, seconds));
        } catch (NumberFormatException e) {
            return fallbackDelay;
        }
    }

    private void sleep(Duration retryDelay) throws IOException {
        try {
            retrySleeper.sleep(retryDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Retry interrupted", e);
        }
    }
}
