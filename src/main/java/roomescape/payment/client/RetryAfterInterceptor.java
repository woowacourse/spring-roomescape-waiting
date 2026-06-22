package roomescape.payment.client;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * 토스가 429 를 주면 Retry-After(초)만큼 기다렸다 재시도하는 인터셉터(클라이언트 관점).
 *
 * <p>maxAttempts 회까지 시도하며, Retry-After 가 없으면 짧은 고정 간격(기본 1초)으로 폴백한다.
 * maxAttempts 를 넘어도 429 면 그 응답을 그대로 돌려준다 → 게이트웨이가 도메인 예외로 변환한다(무한 재시도 금지).
 *
 * <p>429 는 아직 처리되지 않은 상태라 그냥 다시 보내도 안전하다. 같은 요청(body)을 그대로 재실행하므로,
 * {@link TossPaymentGateway} 가 실어 보낸 주문당 고정 멱등키도 재시도 동안 유지된다.
 */
public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {

    private static final long DEFAULT_RETRY_AFTER_SECONDS = 1L;

    private final int maxAttempts;

    public RetryAfterInterceptor(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = execution.execute(request, body);
        int attempt = 1;
        while (isTooManyRequests(response) && attempt < maxAttempts) {
            long waitSeconds = parseRetryAfterSeconds(response);
            response.close();           // 재시도 전 이전 응답 자원 해제
            sleepSeconds(waitSeconds);
            response = execution.execute(request, body);
            attempt++;
        }
        return response;
    }

    private boolean isTooManyRequests(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value();
    }

    private long parseRetryAfterSeconds(ClientHttpResponse response) {
        String value = response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER);
        if (value == null) {
            return DEFAULT_RETRY_AFTER_SECONDS;
        }
        try {
            return Math.max(0, Long.parseLong(value.trim()));
        } catch (NumberFormatException e) {
            return DEFAULT_RETRY_AFTER_SECONDS;
        }
    }

    private void sleepSeconds(long seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("재시도 대기 중 인터럽트되었습니다.", e);
        }
    }
}
