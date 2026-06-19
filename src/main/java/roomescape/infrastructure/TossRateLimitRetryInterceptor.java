package roomescape.infrastructure;

import java.io.IOException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import roomescape.exception.PaymentException.PaymentRateLimitException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class TossRateLimitRetryInterceptor implements ClientHttpRequestInterceptor {

    private final int maxAttempts;
    private final long fallbackIntervalSeconds;

    public TossRateLimitRetryInterceptor(
            @Value("${toss.retry.max-attempts}") int maxAttempts,
            @Value("${toss.retry.fallback-interval-seconds}") long fallbackIntervalSeconds) {
        this.maxAttempts = maxAttempts;
        this.fallbackIntervalSeconds = fallbackIntervalSeconds;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        int attempt = 0;
        while (true) {
            attempt++;
            ClientHttpResponse response = execution.execute(request, body);
            if (response.getStatusCode().value() != HttpStatus.TOO_MANY_REQUESTS.value()) {
                return response;
            }
            if (attempt >= maxAttempts) {
                response.close();
                throw new PaymentRateLimitException(
                        "토스 요청이 한도를 초과(429)해 " + maxAttempts + "회 재시도 후에도 실패했습니다.");
            }
            long waitSeconds = retryAfterSeconds(response).orElse(fallbackIntervalSeconds);
            response.close();
            sleep(waitSeconds);
        }
    }

    private Optional<Long> retryAfterSeconds(ClientHttpResponse response) {
        String header = response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER);
        if (header == null || header.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(header.trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private void sleep(long seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentRateLimitException("토스 429 재시도 대기 중 인터럽트되었습니다.");
        }
    }
}
