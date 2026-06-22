package roomescape.payment.toss;

import java.io.IOException;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * 토스의 429(Too Many Requests)를 가로채 Retry-After만큼 백오프 후 재시도하는 아웃바운드 인터셉터(클라이언트 관점).
 * 429는 아직 처리되지 않은 상태라 그대로 다시 보내도 안전하며, 재시도는 요청에 이미 실린 주문당 고정 멱등키를
 * 유지한 채 같은 (request, body)로 보낸다(read timeout처럼 '됐는지 모름'인 경우까지 중복 승인 방지).
 *
 * <p>Retry-After(초)가 있으면 그만큼, 없으면 짧은 고정 간격(defaultBackoff)으로 대기한다. 시도가 maxAttempts에
 * 도달해도 429면 마지막 응답을 그대로 돌려준다 — 어댑터의 onStatus가 이를 TossPaymentException으로 번역해
 * 도메인 예외로 실패시킨다(무한 재시도 금지). 대기는 BackoffSleeper로 주입해 테스트에서 실제로 자지 않는다.
 *
 * <p>등록 순서상 OutboundRateLimitInterceptor의 안쪽(inner)에 두어, 재시도(execution 재실행)는 매번 실제 전송으로
 * 이어지되 아웃바운드 토큰은 논리적 호출당 한 번만 소비된다.
 */
public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {

    private static final long MILLIS_PER_SECOND = 1000L;

    private final int maxAttempts;
    private final long defaultBackoffMillis;
    private final BackoffSleeper sleeper;

    public RetryAfterInterceptor(int maxAttempts, long defaultBackoffMillis, BackoffSleeper sleeper) {
        this.maxAttempts = maxAttempts;
        this.defaultBackoffMillis = defaultBackoffMillis;
        this.sleeper = sleeper;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        ClientHttpResponse response = execution.execute(request, body);
        int attempt = 1;
        while (isTooManyRequests(response) && attempt < maxAttempts) {
            long backoffMillis = retryAfterMillis(response).orElse(defaultBackoffMillis);
            response.close();
            sleeper.sleep(backoffMillis);
            response = execution.execute(request, body);
            attempt++;
        }
        return response;
    }

    private boolean isTooManyRequests(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value();
    }

    private Optional<Long> retryAfterMillis(ClientHttpResponse response) {
        String retryAfter = response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER);
        if (retryAfter == null || retryAfter.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(retryAfter.trim()) * MILLIS_PER_SECOND);
        } catch (NumberFormatException e) {
            return Optional.empty(); // 초가 아닌 HTTP-date 형식 등은 기본 백오프로 폴백한다.
        }
    }
}
