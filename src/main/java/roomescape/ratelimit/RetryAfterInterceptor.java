package roomescape.ratelimit;

import java.io.IOException;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import roomescape.domain.payment.PaymentConnectionException;

@Component
public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {

    private final int maxAttempts;
    private final Duration defaultDelay;
    private final Sleeper sleeper;

    @Autowired
    public RetryAfterInterceptor(
        @Value("${toss.payments.retry.max-attempts}") int maxAttempts,
        @Value("${toss.payments.retry.default-delay}") Duration defaultDelay
    ) {
        this(maxAttempts, defaultDelay, Thread::sleep);
    }

    RetryAfterInterceptor(int maxAttempts, Duration defaultDelay, Sleeper sleeper) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts는 0보다 커야 합니다.");
        }
        this.maxAttempts = maxAttempts;
        this.defaultDelay = defaultDelay;
        this.sleeper = sleeper;
    }

    @Override
    public ClientHttpResponse intercept(
        HttpRequest request,
        byte[] body,
        ClientHttpRequestExecution execution
    ) throws IOException {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            ClientHttpResponse response = execution.execute(request, body);
            if (response.getStatusCode() != HttpStatus.TOO_MANY_REQUESTS) {
                return response;
            }
            if (attempt == maxAttempts) {
                response.close();
                throw new PaymentRateLimitExceededException();
            }
            Duration delay = retryDelay(response.getHeaders());
            response.close();
            sleep(delay);
        }
        throw new PaymentRateLimitExceededException();
    }

    private Duration retryDelay(HttpHeaders headers) {
        String retryAfter = headers.getFirst(HttpHeaders.RETRY_AFTER);
        if (retryAfter == null || retryAfter.isBlank()) {
            return defaultDelay;
        }
        try {
            return Duration.ofSeconds(Math.max(0, Long.parseLong(retryAfter)));
        } catch (NumberFormatException exception) {
            return defaultDelay;
        }
    }

    private void sleep(Duration delay) {
        try {
            sleeper.sleep(delay);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new PaymentConnectionException();
        }
    }
}
