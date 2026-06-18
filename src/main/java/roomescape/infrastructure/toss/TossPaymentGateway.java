package roomescape.infrastructure.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.exception.PaymentConnectionException;
import roomescape.exception.PaymentUncertainException;

import java.net.SocketTimeoutException;

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

        try {
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

            return new PaymentResult(response.paymentKey(), response.orderId(), response.totalAmount());
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                throw new PaymentUncertainException();
            }
            throw new PaymentConnectionException();
        }
    }
}