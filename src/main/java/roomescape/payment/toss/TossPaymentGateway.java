package roomescape.payment.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.payment.PaymentStatus;
import roomescape.payment.exception.PaymentGatewayException;
import roomescape.payment.toss.dto.TossConfirmRequest;
import roomescape.payment.toss.dto.TossErrorResponse;
import roomescape.payment.toss.dto.TossPaymentResponse;

@Component
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(
            @Qualifier("tossRestClient") RestClient tossRestClient,
            ObjectMapper objectMapper
    ) {
        this.tossRestClient = tossRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        TossConfirmRequest request = new TossConfirmRequest(
                confirmation.paymentKey(), confirmation.orderId(), confirmation.amount());
        TossPaymentResponse response = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (httpRequest, httpResponse) -> {
                    try {
                        TossErrorResponse error = objectMapper.readValue(
                                httpResponse.getBody(), TossErrorResponse.class);
                        throw TossPaymentErrorMapper.map(error.code());
                    } catch (IOException e) {
                        throw new PaymentGatewayException();
                    }
                })
                .body(TossPaymentResponse.class);

        if (response == null) {
            throw new PaymentGatewayException();
        }
        return new PaymentResult(
                response.paymentKey(),
                response.orderId(),
                PaymentStatus.from(response.status()),
                response.totalAmount()
        );
    }
}
