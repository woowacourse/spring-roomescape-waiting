package roomescape.payment.infrastructure.toss;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.payment.exception.PaymentCommunicationException;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

public class TossRateLimitRetryInterceptor implements ClientHttpRequestInterceptor {

    private static final String RATE_LIMIT_EXCEEDED_MESSAGE =
            "결제 승인 요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.";

    private final int maxAttempts;
    private final Duration fallbackBackoff;
    private final BackoffSleeper sleeper;

    public TossRateLimitRetryInterceptor(
            final int maxAttempts,
            final Duration fallbackBackoff,
            final BackoffSleeper sleeper
    ) {
        validate(maxAttempts, fallbackBackoff, sleeper);
        this.maxAttempts = maxAttempts;
        this.fallbackBackoff = fallbackBackoff;
        this.sleeper = sleeper;
    }

    @Override
    public ClientHttpResponse intercept(
            final HttpRequest request,
            final byte[] body,
            final ClientHttpRequestExecution execution
    ) throws IOException {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            final ClientHttpResponse response = execution.execute(request, body);
            if (!response.getStatusCode().isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS)) {
                return response;
            }

            if (attempt == maxAttempts) {
                response.close();
                throw new PaymentCommunicationException(RATE_LIMIT_EXCEEDED_MESSAGE, null);
            }

            final Duration backoff = retryAfter(response)
                    .orElse(fallbackBackoff);
            response.close();
            sleep(backoff);
        }

        throw new PaymentCommunicationException(RATE_LIMIT_EXCEEDED_MESSAGE, null);
    }

    private Optional<Duration> retryAfter(final ClientHttpResponse response) {
        return Optional.ofNullable(response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER))
                .flatMap(this::parseRetryAfter);
    }

    private Optional<Duration> parseRetryAfter(final String retryAfter) {
        try {
            final long seconds = Long.parseLong(retryAfter);
            if (seconds < 0) {
                return Optional.empty();
            }

            return Optional.of(Duration.ofSeconds(seconds));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private void sleep(final Duration backoff) {
        try {
            sleeper.sleep(backoff);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new PaymentCommunicationException(RATE_LIMIT_EXCEEDED_MESSAGE, exception);
        }
    }

    private void validate(
            final int maxAttempts,
            final Duration fallbackBackoff,
            final BackoffSleeper sleeper
    ) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("토스 Rate Limit 재시도 횟수는 1 이상이어야 합니다.");
        }
        if (fallbackBackoff == null || fallbackBackoff.isNegative()) {
            throw new IllegalArgumentException("토스 Rate Limit 기본 대기 시간은 0 이상이어야 합니다.");
        }
        if (sleeper == null) {
            throw new IllegalArgumentException("토스 Rate Limit 대기 전략은 null일 수 없습니다.");
        }
    }
}
