package roomescape.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {
    private final int maxAttempts;
    private final long defaultRetryAfterSeconds;

    public RetryAfterInterceptor(int maxAttempts, long defaultRetryAfterSeconds) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts는 0보다 커야 합니다.");
        }
        if (defaultRetryAfterSeconds < 0) {
            throw new IllegalArgumentException("defaultRetryAfterSeconds는 0 이상이어야 합니다.");
        }

        this.maxAttempts = maxAttempts;
        this.defaultRetryAfterSeconds = defaultRetryAfterSeconds;
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
            long retryAfterSeconds = retryAfterSeconds(response);
            response.close();
            sleep(retryAfterSeconds);
            response = execution.execute(request, body);
            attempt++;
        }

        if (isTooManyRequests(response)) {
            response.close();
            throw new PaymentGatewayException.RateLimited();
        }

        return response;
    }

    private boolean isTooManyRequests(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value();
    }

    private long retryAfterSeconds(ClientHttpResponse response) {
        String retryAfter = response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER);
        if (retryAfter == null || retryAfter.isBlank()) {
            return defaultRetryAfterSeconds;
        }

        try {
            return Math.max(0, Long.parseLong(retryAfter.trim()));
        } catch (NumberFormatException exception) {
            return defaultRetryAfterSeconds;
        }
    }

    private void sleep(long retryAfterSeconds) {
        try {
            Thread.sleep(retryAfterSeconds * 1000L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new PaymentGatewayException.Unknown(exception);
        }
    }
}
