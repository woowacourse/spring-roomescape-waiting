package roomescape.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.client.dto.PaymentConfirmation;
import roomescape.client.dto.TossErrorResponse;
import roomescape.client.dto.TossPaymentResponse;

@Component
public class TossPaymentGateway {

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper) {
        this.tossRestClient = tossRestClient;
        this.objectMapper = objectMapper;
    }

    public TossPaymentResponse confirm(PaymentConfirmation confirmation) {
        record ConfirmRequest(String paymentKey, String orderId, Long amount) {}

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
    }
}
