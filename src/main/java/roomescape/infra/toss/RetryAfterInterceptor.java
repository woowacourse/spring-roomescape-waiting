package roomescape.infra.toss;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {

    private final int maxAttempts;
    private final long fallbackRetryAfterSeconds;

    public RetryAfterInterceptor(int maxAttempts, long fallbackRetryAfterSeconds) {
        this.maxAttempts = maxAttempts;
        this.fallbackRetryAfterSeconds = fallbackRetryAfterSeconds;
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
            sleepSeconds(retryAfterSeconds);
            response = execution.execute(request, body);
            attempt++;
        }

        if (isTooManyRequests(response)) {
            response.close();
            throw new TossPaymentException.TooManyRequests("토스 요청 한도를 초과했습니다.");
        }
        return response;
    }

    private boolean isTooManyRequests(ClientHttpResponse response) throws IOException {
        return response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS;
    }

    private long retryAfterSeconds(ClientHttpResponse response) {
        String retryAfter = response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER);
        if (retryAfter == null || retryAfter.isBlank()) {
            return fallbackRetryAfterSeconds;
        }
        try {
            return Math.max(0, Long.parseLong(retryAfter.trim()));
        } catch (NumberFormatException exception) {
            return fallbackRetryAfterSeconds;
        }
    }

    private void sleepSeconds(long seconds) {
        try {
            Thread.sleep(seconds * 1_000L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new TossPaymentException.Retryable("토스 재시도 대기 중 인터럽트가 발생했습니다.");
        }
    }
}
