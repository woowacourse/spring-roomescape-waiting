package roomescape.client;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClientException;

/**
 * 토스가 429(Too Many Requests)를 주면 Retry-After(초)만큼 기다렸다 재시도하는 인터셉터(클라이언트 관점).
 *
 * <p>Retry-After 헤더가 없으면 짧은 고정 간격(기본 1초)으로 폴백한다. maxAttempts 회까지 시도하고,
 * 그래도 429면 그 응답을 그대로 흘려보내 어댑터의 onStatus(isError) 매핑이 도메인 예외
 * (TossPaymentException)로 실패시킨다 — 무한 재시도하지 않는다.
 * 429는 아직 처리되지 않은 상태라 재시도가 안전하며, 재시도는 호출부가 실어 보낸 멱등키를 그대로 유지한다.
 */
public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {

    private static final long DEFAULT_RETRY_AFTER_SECONDS = 1L;

    private final int maxAttempts;

    public RetryAfterInterceptor(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        // 연결 단계 실패(connect timeout 등)는 execution.execute() 에서 IOException 으로 나가
        // ResourceAccessException 으로 분류된다 — 재시도가 안전한 케이스라 그대로 둔다.
        ClientHttpResponse response = execution.execute(request, body);
        int attempts = 1;
        while (isTooManyRequests(response) && attempts < maxAttempts) {
            long retryAfterSeconds = parseRetryAfterSeconds(response);
            response.close();
            sleepSeconds(retryAfterSeconds);
            response = execution.execute(request, body);
            attempts++;
        }
        return response;
    }

    /**
     * 응답 상태를 읽는다. 상태조차 못 읽는 실패(read timeout 등)는 '연결 실패'가 아니라 '결과 불명확'이므로,
     * IOException 이 ResourceAccessException 으로 reclassify 되지 않게 RestClientException 으로 감싸 던진다
     * (호출부가 read timeout 을 "됐는지 모름"으로 다루는 전제를 유지한다).
     */
    private boolean isTooManyRequests(ClientHttpResponse response) {
        try {
            return response.getStatusCode().isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS);
        } catch (IOException e) {
            throw new RestClientException("토스 응답 상태를 읽지 못했습니다.", e);
        }
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
