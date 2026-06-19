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

    private static final Duration DEFAULT_RETRY_AFTER = Duration.ofSeconds(1);

    private final int maxAttempts;
    private final BackoffSleeper sleeper;
    private final Runnable beforeRetry;

    public RetryAfterInterceptor(int maxAttempts, BackoffSleeper sleeper) {
        this(maxAttempts, sleeper, () -> { });
    }

    public RetryAfterInterceptor(int maxAttempts, BackoffSleeper sleeper, Runnable beforeRetry) {
        this.maxAttempts = maxAttempts;
        this.sleeper = sleeper;
        this.beforeRetry = beforeRetry;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        ClientHttpResponse response = execution.execute(request, body);
        int attempt = 1;
        while (isTooManyRequests(response) && attempt < maxAttempts) {
            Duration retryAfter = retryAfter(response);
            response.close();
            sleeper.sleep(retryAfter);
            beforeRetry.run();
            response = execution.execute(request, body);
            attempt++;
        }
        return response;
    }

    private boolean isTooManyRequests(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value();
    }

    private Duration retryAfter(ClientHttpResponse response) {
        String retryAfter = response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER);
        if (retryAfter == null) {
            return DEFAULT_RETRY_AFTER;
        }
        try {
            return Duration.ofSeconds(Math.max(0L, Long.parseLong(retryAfter.trim())));
        } catch (NumberFormatException e) {
            return DEFAULT_RETRY_AFTER;
        }
    }
}
