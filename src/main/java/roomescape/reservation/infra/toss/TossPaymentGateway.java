package roomescape.reservation.infra.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.global.exception.PaymentGatewayException;
import roomescape.reservation.application.port.out.payment.PaymentConfirmation;
import roomescape.reservation.application.port.out.payment.PaymentGateway;
import roomescape.reservation.application.port.out.payment.PaymentResult;
import roomescape.reservation.infra.toss.dto.ConfirmRequest;
import roomescape.reservation.infra.toss.dto.TossErrorResponse;
import roomescape.reservation.infra.toss.dto.TossPaymentResponse;

@RequiredArgsConstructor
@Component
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

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
                .onStatus(HttpStatusCode::isError, (request1, response1) -> {
                    TossErrorResponse error = objectMapper.readValue(response1.getBody(), TossErrorResponse.class);
                    throw new PaymentGatewayException(error.message());
                })
                .body(TossPaymentResponse.class);

        return response.toResult();
    }
}
