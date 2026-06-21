package roomescape.payment.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.payment.toss.dto.ConfirmRequest;
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
        TossPaymentResponse response = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .body(new ConfirmRequest(confirmation.paymentKey(), confirmation.orderId(), confirmation.amount()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, resp) -> {
                    TossErrorResponse error = objectMapper.readValue(resp.getBody(), TossErrorResponse.class);
                    throw TossPaymentException.of(resp.getStatusCode(), error);
                })
                .body(TossPaymentResponse.class);
        return response.toPaymentResult();
    }
}
