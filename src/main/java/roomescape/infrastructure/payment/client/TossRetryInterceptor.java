package roomescape.infrastructure.payment.client;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.exception.code.PaymentErrorCode;
import roomescape.exception.domain.PaymentException;

public class TossRetryInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TossRetryInterceptor.class);
    static final long DEFAULT_FALLBACK_DELAY_MS = 1_000L;

    private final int maxAttempts;
    private final long fallbackDelayMs;

    public TossRetryInterceptor(int maxAttempts) {
        this(maxAttempts, DEFAULT_FALLBACK_DELAY_MS);
    }

    TossRetryInterceptor(int maxAttempts, long fallbackDelayMs) {
        this.maxAttempts = maxAttempts;
        this.fallbackDelayMs = fallbackDelayMs;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            ClientHttpResponse response = execution.execute(request, body);
            if (response.getStatusCode().value() != HttpStatus.TOO_MANY_REQUESTS.value()) {
                return response;
            }
            handleRateLimitResponse(response, attempt);
        }
        throw new PaymentException(PaymentErrorCode.TOSS_RATE_LIMIT_EXCEEDED);
    }

    private void handleRateLimitResponse(ClientHttpResponse response, int attempt) throws IOException {
        if (attempt >= maxAttempts) {
            log.warn("Toss 429 응답: 최대 재시도 횟수({}) 초과 — 실패 처리", maxAttempts);
            response.close();
            throw new PaymentException(PaymentErrorCode.TOSS_RATE_LIMIT_EXCEEDED);
        }
        long delayMs = extractRetryAfterMs(response);
        log.warn("Toss 429 응답: attempt={}/{}, {}ms 후 재시도 (orderId는 Idempotency-Key로 유지)",
                attempt, maxAttempts, delayMs);
        response.close();
        sleep(delayMs);
    }

    private long extractRetryAfterMs(ClientHttpResponse response) {
        String retryAfter = response.getHeaders().getFirst("Retry-After");
        if (retryAfter == null) {
            log.debug("Retry-After 헤더 없음 — 폴백 대기 {}ms 적용", fallbackDelayMs);
            return fallbackDelayMs;
        }
        try {
            long seconds = Long.parseLong(retryAfter.trim());
            return seconds * 1_000L;
        } catch (NumberFormatException e) {
            log.warn("Retry-After 헤더 파싱 실패: '{}' — 폴백 대기 {}ms 적용", retryAfter, fallbackDelayMs);
            return fallbackDelayMs;
        }
    }

    private void sleep(long ms) {
        if (ms <= 0) {
            return;
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
