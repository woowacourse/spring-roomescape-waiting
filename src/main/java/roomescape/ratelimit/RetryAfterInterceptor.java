package roomescape.ratelimit;

import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.exception.PaymentRateLimitException;

import java.io.IOException;

public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {

    private static final long DEFAULT_RETRY_INTERVAL_MS = 1_000L;

    private final int maxAttempts;

    public RetryAfterInterceptor(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        ClientHttpResponse response = execution.execute(request, body);

        for (int attempt = 1; attempt < maxAttempts && isTooManyRequests(response); attempt++) {
            long waitMs = parseRetryAfterMs(response);
            response.close();
            sleep(waitMs);
            response = execution.execute(request, body);
        }

        if (isTooManyRequests(response)) {
            response.close();
            throw new PaymentRateLimitException();
        }

        return response;
    }

    private boolean isTooManyRequests(ClientHttpResponse response) throws IOException {
        return response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS;
    }

    private long parseRetryAfterMs(ClientHttpResponse response) {
        String retryAfter = response.getHeaders().getFirst("Retry-After");
        if (retryAfter == null) {
            return DEFAULT_RETRY_INTERVAL_MS;
        }
        try {
            return Long.parseLong(retryAfter) * 1_000L;
        } catch (NumberFormatException e) {
            return DEFAULT_RETRY_INTERVAL_MS;
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}