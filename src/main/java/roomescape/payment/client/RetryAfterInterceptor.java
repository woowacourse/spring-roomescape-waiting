package roomescape.payment.client;

import java.io.IOException;
import java.util.function.LongConsumer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {

    private final int maxAttempts;
    private final long fallbackRetryAfterSeconds;
    private final LongConsumer sleeper;

    public RetryAfterInterceptor(int maxAttempts, long fallbackRetryAfterSeconds) {
        this(maxAttempts, fallbackRetryAfterSeconds, RetryAfterInterceptor::sleepSeconds);
    }

    RetryAfterInterceptor(int maxAttempts, long fallbackRetryAfterSeconds, LongConsumer sleeper) {
        this.maxAttempts = maxAttempts;
        this.fallbackRetryAfterSeconds = fallbackRetryAfterSeconds;
        this.sleeper = sleeper;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        int attempt = 1;
        ClientHttpResponse response = execution.execute(request, body);
        while (isTooManyRequests(response)) {
            if (attempt >= maxAttempts) {
                response.close();
                throw new TossPaymentException.RateLimited("결제 승인 서버의 호출 한도를 초과했습니다. 잠시 후 다시 시도해주세요.");
            }
            long waitSeconds = retryAfterSeconds(response);
            response.close();
            sleeper.accept(waitSeconds);
            response = execution.execute(request, body);
            attempt++;
        }
        return response;
    }

    private boolean isTooManyRequests(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value();
    }

    private long retryAfterSeconds(ClientHttpResponse response) {
        String value = response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER);
        if (value == null) {
            return fallbackRetryAfterSeconds;
        }
        try {
            return Math.max(0, Long.parseLong(value.trim()));
        } catch (NumberFormatException e) {
            return fallbackRetryAfterSeconds;
        }
    }

    private static void sleepSeconds(long seconds) {
        try {
            Thread.sleep(seconds * 1_000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("결제 승인 재시도 대기 중 인터럽트되었습니다.", e);
        }
    }
}
