package roomescape.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.client.dto.PaymentConfirmation;
import roomescape.client.dto.TossErrorResponse;
import roomescape.client.dto.TossPaymentResponse;

@Slf4j
@Component
public class TossPaymentGateway {

    private static final int MAX_CONNECTION_ATTEMPTS = 3;
    private static final long CONNECTION_RETRY_DELAY_MS = 300;

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper) {
        this.tossRestClient = tossRestClient;
        this.objectMapper = objectMapper;
    }

    public TossPaymentResponse confirm(PaymentConfirmation confirmation) {
        return withConnectionRetry(() -> doConfirm(confirmation));
    }

    private TossPaymentResponse doConfirm(PaymentConfirmation confirmation) {
        record ConfirmRequest(String paymentKey, String orderId, Long amount) {}

        try {
            return tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ConfirmRequest(confirmation.paymentKey(), confirmation.orderId(), confirmation.amount()))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, resp) -> {
                        var error = objectMapper.readValue(resp.getBody(), TossErrorResponse.class);
                        throw TossPaymentException.of(resp.getStatusCode(), error);
                    })
                    .body(TossPaymentResponse.class);
        } catch (ResourceAccessException e) {
            // 연결 단계 실패 - withConnectionRetry가 재시도 여부를 판단하도록 그대로 전달한다.
            throw e;
        } catch (RestClientException e) {
            // 응답 읽기 단계 실패(느린 응답 등) - 요청은 보냈으나 승인 여부를 확인하지 못한 상태라 재시도하지 않는다.
            throw new TossConfirmResultUnknownException(e);
        }
    }

    /**
     * 연결 단계 실패(ResourceAccessException)만 재시도한다 - 요청이 토스에 도달하지 못했으므로 중복 승인 위험이 없다.
     */
    private <T> T withConnectionRetry(Supplier<T> action) {
        ResourceAccessException lastConnectionFailure = null;
        for (int attempt = 1; attempt <= MAX_CONNECTION_ATTEMPTS; attempt++) {
            try {
                return action.get();
            } catch (ResourceAccessException e) {
                lastConnectionFailure = e;
                log.warn("[토스 연결 재시도] attempt={}/{} message={}", attempt, MAX_CONNECTION_ATTEMPTS, e.getMessage());
                if (attempt < MAX_CONNECTION_ATTEMPTS) {
                    sleep(CONNECTION_RETRY_DELAY_MS);
                }
            }
        }
        // 연결 재시도를 모두 소진한 경우 - 확실히 미승인 상태다.
        throw new TossConnectionException(lastConnectionFailure);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("결제 승인 재시도 중 인터럽트되었습니다.", e);
        }
    }
}
