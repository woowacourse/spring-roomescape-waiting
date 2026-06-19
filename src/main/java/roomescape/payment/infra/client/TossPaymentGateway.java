package roomescape.payment.infra.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.payment.application.dto.PaymentCancel;
import roomescape.payment.application.dto.PaymentConfirmation;
import roomescape.payment.application.PaymentGateway;
import roomescape.payment.application.dto.PaymentResult;
import roomescape.payment.domain.OrderStatus;
import roomescape.payment.infra.client.dto.CancelRequest;
import roomescape.payment.infra.client.dto.ConfirmRequest;
import roomescape.payment.infra.client.dto.TossErrorResponse;
import roomescape.payment.infra.client.dto.TossPaymentResponse;
import roomescape.payment.infra.client.exception.TossExceptionFactory;
import roomescape.payment.infra.client.exception.TossInfrastructureException;
import roomescape.payment.infra.client.exception.TossInfrastructureException.TossConnectionException;
import roomescape.payment.infra.client.exception.TossInfrastructureException.TossTimeoutException;

@Component
@RequiredArgsConstructor
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    @Override
    @Retryable(
            retryFor = TossInfrastructureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public PaymentResult confirm(final PaymentConfirmation confirmation) {
        return executeWithHandling(() -> {
            TossPaymentResponse response = tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", confirmation.orderId())
                    .body(ConfirmRequest.builder()
                            .paymentKey(confirmation.paymentKey())
                            .orderId(confirmation.orderId())
                            .amount(confirmation.amount())
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> handleTossError(res.getStatusCode(), res.getBody()))
                    .body(TossPaymentResponse.class);
            return toResult(response);
        }, "결제 승인");
    }

    @Override
    public PaymentResult cancel(PaymentCancel cancel) {
        return executeWithHandling(() -> {
            TossPaymentResponse response = tossRestClient.post()
                    .uri("/v1/payments/{paymentKey}/cancel", cancel.paymentKey())
                    .body(CancelRequest.builder()
                            .cancelReason(cancel.cancelReason())
                            .cancelAmount(cancel.cancelAmount())
                            .refundReceiveAccount(cancel.refundReceiveAccount())
                            .taxFreeAmount(cancel.taxFreeAmount())
                            .currency(cancel.currency())
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> handleTossError(res.getStatusCode(), res.getBody()))
                    .body(TossPaymentResponse.class);
            return toResult(response);
        }, "결제 취소");
    }

    @Override
    public PaymentResult getStatus(String orderId) {
        return executeWithHandling(() -> {
            TossPaymentResponse response = tossRestClient.get()
                    .uri("/v1/payments/orders/{orderId}", orderId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> handleTossError(res.getStatusCode(), res.getBody()))
                    .body(TossPaymentResponse.class);
            return toResult(response);
        }, "상태 조회");
    }

    private PaymentResult toResult(final TossPaymentResponse response) {
        PaymentStatus tossStatus = PaymentStatus.from(response.status());
        return PaymentResult.builder()
                .paymentKey(response.paymentKey())
                .status(OrderStatus.fromToss(tossStatus))
                .approvedAmount(response.totalAmount())
                .createdAt(OffsetDateTime.parse(response.approvedAt()).toLocalDateTime())
                .build();
    }

    private PaymentResult executeWithHandling(Supplier<PaymentResult> action, String operationName) {
        try {
            return action.get();
        } catch (ResourceAccessException e) {
            throw new TossConnectionException(operationName + " 중 토스 서버 연결 실패");
        } catch (RestClientException e) {
            throw new TossTimeoutException(operationName + " 중 토스 서버 응답 대기 시간 초과");
        }
    }

    private void handleTossError(HttpStatusCode statusCode, java.io.InputStream body) throws java.io.IOException {
        if (statusCode.value() == org.springframework.http.HttpStatus.TOO_MANY_REQUESTS.value()) {
            throw new TossTimeoutException("토스 서버 트래픽 한도 초과로 응답이 지연되었습니다.");
        }
        TossErrorResponse error = objectMapper.readValue(body, TossErrorResponse.class);
        throw TossExceptionFactory.create(statusCode, error);
    }
}
