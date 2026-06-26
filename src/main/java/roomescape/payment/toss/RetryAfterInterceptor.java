package roomescape.payment.toss;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {

    private final int maxAttempts;
    private final Duration defaultBackoff;
    private final Sleeper sleeper;

    public RetryAfterInterceptor(int maxAttempts, Duration defaultBackoff, Sleeper sleeper) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts는 1 이상이어야 합니다. maxAttempts=" + maxAttempts);
        }
        this.maxAttempts = maxAttempts;
        this.defaultBackoff = defaultBackoff;
        this.sleeper = sleeper;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        for (int attempt = 1; ; attempt++) {
            ClientHttpResponse response = execution.execute(request, body);
            if (response.getStatusCode().value() != HttpStatus.TOO_MANY_REQUESTS.value()) {
                return response;
            }
            if (attempt >= maxAttempts) {
                response.close();
                throw new TossRateLimitException("토스 호출이 요청 한도(429)를 초과했습니다. attempts=" + attempt);
            }
            Duration backoff = retryAfter(response).orElse(defaultBackoff);
            response.close();
            sleeper.sleep(backoff);
        }
    }

    private Optional<Duration> retryAfter(ClientHttpResponse response) throws IOException {
        String value = response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER);
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Duration.ofSeconds(Long.parseLong(value.trim())));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
