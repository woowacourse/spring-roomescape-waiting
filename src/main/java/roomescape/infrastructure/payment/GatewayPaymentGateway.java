package roomescape.infrastructure.payment;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentResult;
import roomescape.domain.PaymentStatus;
import roomescape.infrastructure.payment.dto.ConfirmRequest;
import roomescape.infrastructure.payment.dto.GatewayConfirmResponse;
import roomescape.service.PaymentGateway;

/**
 * PaymentGateway 포트의 게이트웨이 구현(어댑터). 나가는 한도와 백오프가 걸린 gatewayRestClient 로 호출하지만, 도메인은 그 사실을 모른 채 포트만 쓴다.
 */
@Component
public class GatewayPaymentGateway implements PaymentGateway {

    private final RestClient gatewayRestClient;

    public GatewayPaymentGateway(RestClient gatewayRestClient) {
        this.gatewayRestClient = gatewayRestClient;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        var request = new ConfirmRequest(
                confirmation.paymentKey(), confirmation.orderId(), confirmation.amount());
        var response = gatewayRestClient.post()
                .uri("/v1/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(GatewayConfirmResponse.class);
        return new PaymentResult(
                response.paymentKey(),
                response.orderId(),
                PaymentStatus.from(response.status()),
                response.totalAmount()
        );
    }

}
