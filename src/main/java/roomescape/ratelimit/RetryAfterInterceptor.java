package roomescape.ratelimit;

import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;

@RequiredArgsConstructor
public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {

    private final int maxAttempts;
    private final Duration fallbackRetryAfter;
    private final Sleeper sleeper;

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
                throw new EscapeRoomException(ErrorCode.PAYMENT_GATEWAY_RATE_LIMITED);
            }

            Duration retryAfter = retryAfter(response.getHeaders());
            response.close();
            sleep(retryAfter);
            attempt++;
        }
    }

    private Duration retryAfter(HttpHeaders headers) {
        String retryAfter = headers.getFirst(HttpHeaders.RETRY_AFTER);
        if (retryAfter == null) {
            return fallbackRetryAfter;
        }
        try {
            long seconds = Long.parseLong(retryAfter);
            if (seconds < 0) {
                return fallbackRetryAfter;
            }
            return Duration.ofSeconds(seconds);
        } catch (NumberFormatException exception) {
            return fallbackRetryAfter;
        }
    }

    private void sleep(Duration duration) {
        try {
            sleeper.sleep(duration);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new EscapeRoomException(ErrorCode.PAYMENT_GATEWAY_ERROR);
        }
    }
}
