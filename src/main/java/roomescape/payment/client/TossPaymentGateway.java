package roomescape.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.payment.client.dto.ConfirmRequest;
import roomescape.payment.client.dto.TossErrorResponse;
import roomescape.payment.client.dto.TossPaymentResponse;

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
        TossPaymentResponse tossResponse = Objects.requireNonNull(
                tossRestClient.post()
                        .uri("/v1/payments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new ConfirmRequest(confirmation.paymentKey(), confirmation.orderId(), confirmation.amount()))
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, (request, response) -> {
                            TossErrorResponse error = objectMapper.readValue(response.getBody(), TossErrorResponse.class);
                            throw TossPaymentException.of(response.getStatusCode(), error);
                        })
                        .body(TossPaymentResponse.class)
        );
        return new PaymentResult(tossResponse.paymentKey(), tossResponse.orderId(), tossResponse.totalAmount());
    }
}