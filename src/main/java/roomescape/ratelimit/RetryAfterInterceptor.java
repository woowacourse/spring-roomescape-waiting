package roomescape.ratelimit;

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
    private final Duration fallbackDelay;
    private final Sleeper sleeper;

    public RetryAfterInterceptor(RetryAfterProperties properties) {
        this(properties.getMaxAttempts(), properties.getFallbackDelay(), duration -> Thread.sleep(duration.toMillis()));
    }

    public RetryAfterInterceptor(int maxAttempts, Duration fallbackDelay, Sleeper sleeper) {
        if (maxAttempts <= 0) {
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
        int attempt = 1;
        while (true) {
            ClientHttpResponse response = execution.execute(request, body);
            if (response.getStatusCode() != HttpStatus.TOO_MANY_REQUESTS) {
                return response;
            }
            if (attempt >= maxAttempts) {
                response.close();
                throw new RetryAfterExceededException();
            }

            Duration delay = retryAfter(response.getHeaders());
            response.close();
            sleep(delay);
            attempt++;
        }
    }

    private Duration retryAfter(HttpHeaders headers) {
        String retryAfter = headers.getFirst(HttpHeaders.RETRY_AFTER);
        if (retryAfter == null || retryAfter.isBlank()) {
            return fallbackDelay;
        }
        try {
            return Duration.ofSeconds(Math.max(0, Long.parseLong(retryAfter)));
        } catch (NumberFormatException e) {
            return fallbackDelay;
        }
    }

    private void sleep(Duration delay) throws IOException {
        try {
            sleeper.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Retry-After 대기 중 인터럽트가 발생했습니다.", e);
        }
    }
}
