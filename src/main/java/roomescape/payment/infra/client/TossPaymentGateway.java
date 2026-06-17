package roomescape.payment.infra.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import roomescape.payment.application.dto.PaymentCancel;
import roomescape.payment.application.dto.PaymentConfirmation;
import roomescape.payment.application.PaymentGateway;
import roomescape.payment.application.dto.PaymentResult;
import roomescape.payment.infra.client.dto.CancelRequest;
import roomescape.payment.infra.client.dto.ConfirmRequest;
import roomescape.payment.infra.client.dto.TossErrorResponse;
import roomescape.payment.infra.client.dto.TossPaymentResponse;
import roomescape.payment.infra.client.exception.TossExceptionFactory;
import roomescape.payment.infra.client.exception.TossInfrastructureException;
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
        try {
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
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        TossErrorResponse error = objectMapper.readValue(res.getBody(), TossErrorResponse.class);
                        throw TossExceptionFactory.create(res.getStatusCode(), error);
                    }).body(TossPaymentResponse.class);
            return toResult(response);
        } catch (ResourceAccessException e) {
            throw new TossTimeoutException("토스 서버 응답 대기 시간 초과");
        }
    }

    @Override
    public PaymentResult cancel(PaymentCancel cancel) {
        CancelRequest request = CancelRequest.builder()
                .cancelReason(cancel.cancelReason())
                .cancelAmount(cancel.cancelAmount())
                .refundReceiveAccount(cancel.refundReceiveAccount())
                .taxFreeAmount(cancel.taxFreeAmount())
                .currency(cancel.currency())
                .build();

        TossPaymentResponse response = tossRestClient.post()
                .uri("/v1/payments/{paymentKey}/cancel", cancel.paymentKey())
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    TossErrorResponse error = objectMapper.readValue(res.getBody(), TossErrorResponse.class);
                    throw TossExceptionFactory.create(res.getStatusCode(), error);
                })
                .body(TossPaymentResponse.class);
        return toResult(response);
    }

    @Override
    public PaymentResult getStatus(String orderId) {
        TossPaymentResponse response = tossRestClient.get()
                .uri("/v1/payments/orders/{orderId}", orderId)
                .retrieve()
                .body(TossPaymentResponse.class);
        return toResult(response);
    }

    private PaymentResult toResult(final TossPaymentResponse response) {
        return PaymentResult.builder()
                .paymentKey(response.paymentKey())
                .status(PaymentStatus.from(response.status()))
                .approvedAmount(response.totalAmount())
                .createdAt(OffsetDateTime.parse(response.approvedAt()).toLocalDateTime())
                .build();
    }
}
