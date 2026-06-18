package roomescape.infrastructure.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentResult;
import roomescape.service.payment.port.PaymentGateway;

@Component
public class TossPaymentGateway implements PaymentGateway {

    private static final String CONFIRM_URI = "/v1/payments/confirm";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(
            @Qualifier("tossRestClient") RestClient restClient,
            ObjectMapper objectMapper
    ) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        TossConfirmResponse response;
        try {
            response = restClient.post()
                    .uri(CONFIRM_URI)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", confirmation.orderId())
                    .body(new TossConfirmRequest(confirmation.paymentKey(), confirmation.orderId(),
                            confirmation.amount()))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, clientResponse) -> {
                        TossErrorResponse errorResponse = readErrorResponse(clientResponse.getBody().readAllBytes());
                        throw TossPaymentException.of(clientResponse.getStatusCode(), errorResponse);
                    })
                    .body(TossConfirmResponse.class);
        } catch (RestClientException e) {
            throw TossPaymentException.fromNetworkFailure(e);
        }

        return new PaymentResult(response.paymentKey(), response.orderId(), response.totalAmount());
    }

    private TossErrorResponse readErrorResponse(byte[] body) throws IOException {
        return objectMapper.readValue(body, TossErrorResponse.class);
    }
}
