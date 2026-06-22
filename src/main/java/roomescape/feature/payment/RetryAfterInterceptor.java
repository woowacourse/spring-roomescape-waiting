package roomescape.feature.payment;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * 토스가 429(Too Many Requests)를 응답하면 {@code Retry-After} 만큼 대기한 뒤 재시도해 최종 정상 응답을 받는다.
 *
 * <ul>
 *   <li>{@code Retry-After} 헤더(delta-seconds)가 있으면 그만큼 대기한다.</li>
 *   <li>헤더가 없거나 해석할 수 없으면 짧은 고정 간격({@code fallbackDelay}, 기본 1초)으로 폴백한다.</li>
 *   <li>한 요청을 과도하게 붙잡지 않도록 대기 시간은 {@link #MAX_DELAY} 로 상한을 둔다.</li>
 *   <li>{@code maxAttempts} 회까지 시도해도 429면 {@link PaymentRateLimitedException} 으로 실패한다.</li>
 * </ul>
 *
 * 전송 계층(RestClient 인터셉터)에서 처리하므로 호출부({@link TossPaymentClient}, {@link PaymentApprover})는
 * 429 분기를 알 필요가 없다. 단, 부착 지점은 {@link TossPaymentClient} 안에 있어 존재가 드러난다.
 */
public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {

    private static final Duration MAX_DELAY = Duration.ofSeconds(60);

    private final Sleeper sleeper;
    private final int maxAttempts;
    private final Duration fallbackDelay;

    public RetryAfterInterceptor(Sleeper sleeper, int maxAttempts, Duration fallbackDelay) {
        this.sleeper = sleeper;
        this.maxAttempts = maxAttempts;
        this.fallbackDelay = fallbackDelay;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        ClientHttpResponse response = execution.execute(request, body);

        int attempt = 1;
        while (isTooManyRequests(response) && attempt < maxAttempts) {
            Duration delay = retryAfterOrFallback(response);
            response.close();
            sleeper.sleep(delay);
            response = execution.execute(request, body);
            attempt++;
        }

        if (isTooManyRequests(response)) {
            Duration retryAfter = retryAfterOrFallback(response);
            response.close();
            throw new PaymentRateLimitedException(maxAttempts, retryAfter.toSeconds());
        }
        return response;
    }

    private boolean isTooManyRequests(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value();
    }

    private Duration retryAfterOrFallback(ClientHttpResponse response) {
        Duration delay = parseDeltaSeconds(response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER))
                .orElse(fallbackDelay);

        return delay.compareTo(MAX_DELAY) > 0 ? MAX_DELAY : delay;
    }

    private Optional<Duration> parseDeltaSeconds(String header) {
        if (header == null || header.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Duration.ofSeconds(Long.parseLong(header.trim())));
        } catch (NumberFormatException e) {
            // delta-seconds 가 아닌 형식(HTTP-date 등)은 폴백 간격으로 처리한다.
            return Optional.empty();
        }
    }
}
