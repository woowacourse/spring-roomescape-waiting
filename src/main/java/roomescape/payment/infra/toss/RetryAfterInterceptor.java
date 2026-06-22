package roomescape.payment.infra.toss;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.payment.domain.exception.PaymentRetryableException;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Consumer;

public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {
    private final int maxAttempts;
    private final Duration fallbackDelay;
    private final Consumer<Duration> sleeper;

    public RetryAfterInterceptor(int maxAttempts, Duration fallbackDelay) {
        this(maxAttempts, fallbackDelay, RetryAfterInterceptor::sleep);
    }

    RetryAfterInterceptor(int maxAttempts, Duration fallbackDelay, Consumer<Duration> sleeper) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts는 1 이상이어야 합니다.");
        }
        if (fallbackDelay.isNegative()) {
            throw new IllegalArgumentException("fallbackDelay는 음수일 수 없습니다.");
        }

        this.maxAttempts = maxAttempts;
        this.fallbackDelay = fallbackDelay;
        this.sleeper = sleeper;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        ClientHttpResponse response = execution.execute(request, body);
        int attempt = 1;

        while (isTooManyRequests(response) && attempt < maxAttempts) {
            Duration delay = retryAfter(response);
            response.close();
            sleeper.accept(delay);
            response = execution.execute(request, body);
            attempt++;
        }

        return response;
    }

    private boolean isTooManyRequests(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value();
    }

    private Duration retryAfter(ClientHttpResponse response) {
        String value = response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER);
        if (value == null || value.isBlank()) {
            return fallbackDelay;
        }

        try {
            return Duration.ofSeconds(Math.max(0, Long.parseLong(value.trim())));
        } catch (NumberFormatException exception) {
            return fallbackDelay;
        }
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new PaymentRetryableException("결제 승인 재시도 대기 중 인터럽트되었습니다.");
        }
    }
}
