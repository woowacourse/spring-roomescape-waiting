package roomescape.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.client.dto.ConfirmRequest;
import roomescape.client.dto.TossErrorResponse;
import roomescape.client.dto.TossPaymentResponse;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentResult;
import roomescape.service.PaymentGateway;

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
        ConfirmRequest request = new ConfirmRequest(
                confirmation.paymentKey(),
                confirmation.orderId(),
                confirmation.amount()
        );

        TossPaymentResponse response = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (httpRequest, clientHttpResponse) -> {
                    TossErrorResponse error = objectMapper.readValue(clientHttpResponse.getBody(), TossErrorResponse.class);
                    throw TossPaymentException.of(clientHttpResponse.getStatusCode(), error);
                })
                .body(TossPaymentResponse.class);

        return new PaymentResult(response.paymentKey(), response.orderId(), response.totalAmount());
    }
}
