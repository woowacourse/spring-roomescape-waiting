package roomescape.payment.infrastructure;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import roomescape.common.config.OutboundRateLimitProperties;
import roomescape.common.exception.RoomEscapeException;
import roomescape.payment.exception.PaymentErrorCode;

/**
 * 토스가 429를 주면 Retry-After(초)만큼 대기 후 재시도한다. 헤더가 없으면 짧은 고정 간격으로 폴백한다.
 * maxAttempts를 넘어도 429면 도메인 예외로 실패시킨다(무한 재시도 금지).
 * 멱등키는 같은 요청을 그대로 재전송하므로 자동으로 유지된다.
 */
@Component
public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RetryAfterInterceptor.class);

    private final int maxAttempts;
    private final long fallbackSeconds;

    public RetryAfterInterceptor(OutboundRateLimitProperties properties) {
        this.maxAttempts = properties.maxAttempts();
        this.fallbackSeconds = properties.fallbackSeconds();
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        for (int attempt = 1; ; attempt++) {
            ClientHttpResponse response = execution.execute(request, body);
            if (response.getStatusCode().value() != HttpStatus.TOO_MANY_REQUESTS.value()) {
                return response;
            }
            if (attempt >= maxAttempts) {
                log.warn("토스 429 재시도 한도 초과 - attempts={}", attempt);
                response.close();
                throw new RoomEscapeException(PaymentErrorCode.PAYMENT_RATE_LIMITED);
            }
            long waitSeconds = parseRetryAfter(response);
            response.close();
            sleep(waitSeconds);
        }
    }

    private long parseRetryAfter(ClientHttpResponse response) {
        String retryAfter = response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER);
        if (retryAfter == null) {
            return fallbackSeconds;
        }
        try {
            return Long.parseLong(retryAfter.trim());
        } catch (NumberFormatException invalidFormat) {
            return fallbackSeconds;
        }
    }

    private void sleep(long seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RoomEscapeException(PaymentErrorCode.PAYMENT_RATE_LIMITED);
        }
    }
}
