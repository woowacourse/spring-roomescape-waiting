package roomescape.infra.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.domain.payment.PaymentStatus;
import roomescape.exception.PaymentConfirmationUnknownException;
import roomescape.exception.PaymentConnectionException;
import roomescape.exception.PaymentGatewayException;
import roomescape.infra.toss.dto.ConfirmRequest;
import roomescape.infra.toss.dto.TossErrorResponse;
import roomescape.infra.toss.dto.TossPaymentResponse;

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

            return toResult(response);
        } catch (RestClientException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ConnectException) {
                throw new PaymentConnectionException(e);
            }
            if (cause instanceof SocketTimeoutException) {
                throw new PaymentConfirmationUnknownException(e);
            }
            throw new PaymentGatewayException(e);
        }
    }

    private PaymentResult toResult(TossPaymentResponse response) {
        return new PaymentResult(
                response.paymentKey(),
                response.orderId(),
                PaymentStatus.from(response.status()),
                response.totalAmount()
        );
    }
}
