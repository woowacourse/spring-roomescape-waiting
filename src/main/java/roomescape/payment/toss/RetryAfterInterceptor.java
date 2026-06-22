package roomescape.payment.toss;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Consumer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

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
        int attempts = 1;
        while (isTooManyRequests(response) && attempts < maxAttempts) {
            Duration delay = retryAfter(response);
            response.close();
            sleeper.accept(delay);
            response = execution.execute(request, body);
            attempts++;
        }
        return response;
    }

    private boolean isTooManyRequests(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS);
    }

    private Duration retryAfter(ClientHttpResponse response) {
        String value = response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER);
        if (value == null) {
            return fallbackDelay;
        }
        try {
            long seconds = Math.max(0, Long.parseLong(value.trim()));
            return Duration.ofSeconds(seconds);
        } catch (NumberFormatException e) {
            return fallbackDelay;
        }
    }

    private static void sleep(Duration delay) {
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("토스 결제 재시도 대기 중 인터럽트되었습니다.", e);
        }
    }
}
