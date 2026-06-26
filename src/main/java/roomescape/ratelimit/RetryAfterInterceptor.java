package roomescape.ratelimit;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.payment.TossRateLimitException;

public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {

    private final int maxAttempts;
    private final long fallbackSeconds;

    public RetryAfterInterceptor(int maxAttempts, long fallbackSeconds) {
        this.maxAttempts = maxAttempts;
        this.fallbackSeconds = fallbackSeconds;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        ClientHttpResponse response = execution.execute(request, body);
        int attempt = 1;
        while (isTooManyRequests(response) && attempt < maxAttempts) {
            long waitSeconds = retryAfterSeconds(response);
            response.close();
            sleep(waitSeconds);
            response = execution.execute(request, body);
            attempt++;
        }
        if (isTooManyRequests(response)) {
            long retryAfter = retryAfterSeconds(response);
            response.close();
            throw new TossRateLimitException(retryAfter);
        }
        return response;
    }

    private boolean isTooManyRequests(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value();
    }

    private long retryAfterSeconds(ClientHttpResponse response) {
        String header = response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER);
        if (header == null) {
            return fallbackSeconds;
        }
        try {
            return Long.parseLong(header.trim());
        } catch (NumberFormatException e) {
            return fallbackSeconds;
        }
    }

    private void sleep(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TossRateLimitException(seconds);
        }
    }
}
