package roomescape.payment.infra.toss;

import java.io.IOException;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.payment.application.exception.PaymentErrorCode;
import roomescape.payment.application.exception.PaymentException;

public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {

    private final int maxAttempts;
    private final Duration fallbackDelay;
    private final Sleeper sleeper;
    private final Runnable beforeRetry;

    public RetryAfterInterceptor(int maxAttempts, Duration fallbackDelay) {
        this(maxAttempts, fallbackDelay, Thread::sleep, () -> {
        });
    }

    public RetryAfterInterceptor(
            int maxAttempts,
            Duration fallbackDelay,
            Runnable beforeRetry
    ) {
        this(maxAttempts, fallbackDelay, Thread::sleep, beforeRetry);
    }

    RetryAfterInterceptor(int maxAttempts, Duration fallbackDelay, Sleeper sleeper) {
        this(maxAttempts, fallbackDelay, sleeper, () -> {
        });
    }

    RetryAfterInterceptor(
            int maxAttempts,
            Duration fallbackDelay,
            Sleeper sleeper,
            Runnable beforeRetry
    ) {
        this.maxAttempts = maxAttempts;
        this.fallbackDelay = fallbackDelay;
        this.sleeper = sleeper;
        this.beforeRetry = beforeRetry;
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
                throw new PaymentException(PaymentErrorCode.GATEWAY_RATE_LIMIT_EXCEEDED);
            }

            Duration delay = retryDelay(response.getHeaders());
            response.close();
            sleep(delay);
            beforeRetry.run();
        }
        throw new PaymentException(PaymentErrorCode.GATEWAY_RATE_LIMIT_EXCEEDED);
    }

    private Duration retryDelay(HttpHeaders headers) {
        String retryAfter = headers.getFirst(HttpHeaders.RETRY_AFTER);
        if (retryAfter == null) {
            return fallbackDelay;
        }
        try {
            long seconds = Long.parseLong(retryAfter.trim());
            if (seconds < 0) {
                return fallbackDelay;
            }
            return Duration.ofSeconds(seconds);
        } catch (NumberFormatException exception) {
            return fallbackDelay;
        }
    }

    private void sleep(Duration delay) {
        try {
            sleeper.sleep(delay.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new PaymentException(PaymentErrorCode.GATEWAY_RETRY_INTERRUPTED);
        }
    }

    @FunctionalInterface
    interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }
}
