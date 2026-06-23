package roomescape.payment.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.domain.PaymentStatus;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.payment.client.dto.ConfirmRequest;
import roomescape.payment.client.dto.TossPaymentResponse;

@Component
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient tossRestClient;

    public TossPaymentGateway(RestClient tossRestClient) {
        this.tossRestClient = tossRestClient;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        ConfirmRequest request = new ConfirmRequest(
                confirmation.paymentKey(), confirmation.orderId(), confirmation.amount());
        TossPaymentResponse response = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(TossPaymentResponse.class);
        return new PaymentResult(
                response.paymentKey(),
                response.orderId(),
                PaymentStatus.fromTossStatus(response.status()),
                response.totalAmount()
        );
    }
}
