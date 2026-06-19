package roomescape.payment.toss;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.payment.toss.dto.TossConfirmRequest;
import roomescape.payment.toss.dto.TossErrorResponse;
import roomescape.payment.toss.dto.TossPaymentResponse;

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
        TossConfirmRequest request = new TossConfirmRequest(
                confirmation.paymentKey(),
                confirmation.orderId(),
                confirmation.amount()
        );
        TossPaymentResponse response = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    TossErrorResponse error = objectMapper.readValue(res.getBody(), TossErrorResponse.class);
                    throw TossPaymentException.of(res.getStatusCode(), error);
                })
                .body(TossPaymentResponse.class);
        return toResult(response);
    }

    private PaymentResult toResult(TossPaymentResponse response) {
        return new PaymentResult(
                response.paymentKey(),
                response.orderId(),
                response.status(),
                response.totalAmount()
        );
    }
}
