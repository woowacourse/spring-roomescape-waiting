package roomescape.client.toss;

import java.io.IOException;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.client.PaymentGatewayRetryableException;

public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {

    private final int maxAttempts;
    private final Duration fallbackDelay;

    public RetryAfterInterceptor(int maxAttempts, Duration fallbackDelay) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts는 1 이상이어야 합니다.");
        }
        this.maxAttempts = maxAttempts;
        this.fallbackDelay = fallbackDelay;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        int attempt = 1;
        ClientHttpResponse response = execution.execute(request, body);
        while (isTooManyRequests(response) && attempt < maxAttempts) {
            Duration delay = retryDelay(response);
            response.close();
            sleep(delay);
            response = execution.execute(request, body);
            attempt++;
        }
        if (isTooManyRequests(response)) {
            response.close();
            throw new PaymentGatewayRetryableException("TOSS_RATE_LIMIT_EXCEEDED", "결제 API 호출 한도를 초과했습니다.");
        }
        return response;
    }

    private boolean isTooManyRequests(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value();
    }

    private Duration retryDelay(ClientHttpResponse response) {
        String retryAfter = response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER);
        if (retryAfter == null) {
            return fallbackDelay;
        }
        try {
            long seconds = Long.parseLong(retryAfter.trim());
            return Duration.ofSeconds(Math.max(0, seconds));
        } catch (NumberFormatException e) {
            return fallbackDelay;
        }
    }

    private void sleep(Duration delay) {
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentGatewayRetryableException("TOSS_RETRY_INTERRUPTED", "결제 API 재시도 대기 중 인터럽트되었습니다.", e);
        }
    }
}
