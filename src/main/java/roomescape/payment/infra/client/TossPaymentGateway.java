package roomescape.payment.infra.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.payment.application.dto.PaymentCancel;
import roomescape.payment.application.dto.PaymentConfirmation;
import roomescape.payment.application.PaymentGateway;
import roomescape.payment.application.dto.PaymentResult;
import roomescape.payment.infra.client.dto.CancelRequest;
import roomescape.payment.infra.client.dto.ConfirmRequest;
import roomescape.payment.infra.client.dto.TossErrorResponse;
import roomescape.payment.infra.client.dto.TossPaymentResponse;
import roomescape.payment.infra.client.exception.TossPaymentException;

@Component
@RequiredArgsConstructor
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    @Override
    public PaymentResult confirm(final PaymentConfirmation confirmation) {
        ConfirmRequest request = ConfirmRequest.builder()
                .paymentKey(confirmation.paymentKey())
                .orderId(confirmation.orderId())
                .amount(confirmation.amount())
                .build();
        TossPaymentResponse response = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", confirmation.orderId())
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    TossErrorResponse error = objectMapper.readValue(res.getBody(), TossErrorResponse.class);
                    throw TossPaymentException.of(res.getStatusCode(), error);
                }).body(TossPaymentResponse.class);
        return toResult(response);
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
                    throw TossPaymentException.of(res.getStatusCode(), error);
                })
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
