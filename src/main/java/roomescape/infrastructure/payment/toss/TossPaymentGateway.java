package roomescape.infrastructure.payment.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.domain.order.PaymentStatus;
import roomescape.infrastructure.payment.PaymentConfirmation;
import roomescape.infrastructure.payment.toss.dto.ConfirmRequest;
import roomescape.infrastructure.payment.toss.dto.TossErrorResponse;
import roomescape.infrastructure.payment.toss.dto.TossPaymentResponse;
import roomescape.service.PaymentGateway;
import roomescape.service.dto.result.PaymentResult;

@Component
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper) {
        this.tossRestClient = tossRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        ConfirmRequest request = objectMapper.convertValue(confirmation, ConfirmRequest.class);
        TossPaymentResponse response = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .header("Idempotency-Key", confirmation.orderId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    TossErrorResponse error = objectMapper.readValue(res.getBody(), TossErrorResponse.class);
                    throw TossPaymentException.of(res.getStatusCode(), error);
                })
                .body(TossPaymentResponse.class);

        return new PaymentResult(
                response.paymentKey(),
                response.orderId(),
                PaymentStatus.valueOf(response.status()),
                response.totalAmount()
        );
    }
}
