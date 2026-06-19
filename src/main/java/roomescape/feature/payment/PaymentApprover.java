package roomescape.feature.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import java.net.ConnectException;
import java.net.UnknownHostException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler;
import org.springframework.web.client.RestClientException;
import roomescape.feature.payment.dto.PaymentApproveRequest;
import roomescape.feature.payment.dto.PaymentErrorResponse;
import roomescape.feature.payment.dto.PaymentResponse;
import roomescape.global.ratelimit.OutboundRateLimit;

@RequiredArgsConstructor
@Component
public class PaymentApprover {

    private static final String APPROVE_URI = "/v1/payments/confirm";
    private static final String PAYMENT_STATUS_URI = "/v1/payments/{paymentKey}";
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private static final int MAX_ATTEMPTS = 3;
    private static final long BACKOFF_DELAY_MILLIS = 200L;
    private static final double BACKOFF_MULTIPLIER = 2.0;

    private final RestClient paymentRestClient;
    private final ObjectMapper objectMapper;

    /**
     * 토스 결제 승인을 요청한다.
     *
     * 전송 계층 실패({@link PaymentClientException})는 토스가 Idempotency-Key 로 안전한 재시도를 보장하므로 재시도한다.
     * 연결 실패와 읽기 타임아웃은 재시도를 공유하되, 재시도 소진 후의 처리는 {@link #recover} 에서 갈린다.
     * 토스가 에러 코드를 응답한 경우({@link PaymentException})는 비즈니스 결과이므로 재시도하지 않는다.
     *
     * Idempotency-Key 는 재시도마다 동일해야 하므로 주문 단위로 고정된 orderId 를 사용한다.
     */
    @Retryable(
            retryFor = PaymentClientException.class,
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = BACKOFF_DELAY_MILLIS, multiplier = BACKOFF_MULTIPLIER)
    )
    @OutboundRateLimit(key = "payment_outbound")
    public boolean approve(PaymentApproveRequest request) {
        try {
            PaymentResponse response = paymentRestClient.post()
                    .uri(APPROVE_URI)
                    .header(IDEMPOTENCY_KEY_HEADER, request.orderId())
                    .body(request)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, errorHandler())
                    .body(PaymentResponse.class);

            return isApproved(response);
        } catch (PaymentException e) {
            if (e.getFailureType() == PaymentFailureType.ALREADY_DONE) {
                // 멱등: 이미 처리된 결제는 승인된 것으로 간주한다. (토스 승인 성공 후 DB 반영 실패 복구 등)
                return true;
            }
            throw e;
        } catch (ResourceAccessException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ConnectException || cause instanceof UnknownHostException) {
                // TCP 연결 자체가 수립되지 않음 → 요청이 전송된 적 없음 → 미청구 확정.
                throw new PaymentConnectionException(e);
            }
            // 읽기 타임아웃·연결 리셋 등: 요청 전송 여부 불명 → 보수적으로 결과 불명 처리.
            throw new PaymentTimeoutException(e);
        } catch (RestClientException e) {
            throw new PaymentTimeoutException(e);
        }
    }

    /**
     * {@link Retryable} 의 종료 지점 처리.
     *
     * 주의: spring-retry 는 @Recover 가 존재하면, 재시도가 소진됐을 때뿐 아니라
     * '재시도 대상이 아닌 예외'(예: 비즈니스 결과인 {@link PaymentException})가 던져졌을 때도 이 메서드로 위임한다.
     * 따라서 모든 종료 예외(RuntimeException)를 받아, 읽기 타임아웃만 별도로 처리하고 나머지는 원래 예외 그대로 다시 던진다.
     * (만약 PaymentClientException 만 받으면, PaymentException 종료 시 매칭되는 recover 가 없어
     *  ExhaustedRetryException 으로 감싸지며 원래 비즈니스 에러가 사라진다.)
     *
     * <ul>
     *   <li>읽기 타임아웃: 결과가 불명이므로 실패로 단정하지 않고 실제 결제 상태를 조회(reconciliation)한다.
     *       조회 결과가 DONE 이면 실제로는 승인이 완료된 것이므로 true 를 반환해 예약 확정으로 진행하고,
     *       그래도 확정하지 못하면 예외를 그대로 던져 '확인 중'(504)으로 표면화한다.</li>
     *   <li>연결 실패: 요청이 전송된 적 없어 '미청구'가 확정이므로 그대로 던져(503) 결제 실패로 처리한다.</li>
     *   <li>그 외(비즈니스 에러 등): 재시도 없이 원래 예외를 그대로 전파한다.</li>
     * </ul>
     */
    @Recover
    boolean recover(RuntimeException e, PaymentApproveRequest request) {
        if (e instanceof PaymentTimeoutException && queryPaymentStatus(request.paymentKey()) == PaymentStatus.DONE) {
            return true;
        }
        throw e;
    }

    private PaymentStatus queryPaymentStatus(String paymentKey) {
        try {
            PaymentResponse response = paymentRestClient.get()
                    .uri(PAYMENT_STATUS_URI, paymentKey)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, errorHandler())
                    .body(PaymentResponse.class);

            return response == null ? null : response.status();
        } catch (RuntimeException e) {
            // 상태 조회마저 실패하면 결과는 여전히 불명 → null 로 '모름'을 표현한다.
            return null;
        }
    }

    private boolean isApproved(PaymentResponse response) {
        return response != null && response.status() == PaymentStatus.DONE;
    }

    private ErrorHandler errorHandler() {
        return (request, response) -> {
            PaymentErrorResponse errorResponse = objectMapper.readValue(response.getBody(), PaymentErrorResponse.class);
            PaymentFailureType failureType = PaymentFailureType.from(errorResponse.code());

            throw new PaymentException(failureType, errorResponse.code(), errorResponse.message());
        };
    }
}
