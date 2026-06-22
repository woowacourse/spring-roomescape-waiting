package roomescape.infrastructure.payment.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.application.payment.PaymentGateway;
import roomescape.application.payment.model.PaymentConfirmation;
import roomescape.application.payment.model.PaymentResult;
import roomescape.application.payment.model.PaymentStatus;
import roomescape.infrastructure.payment.toss.dto.ConfirmRequest;
import roomescape.infrastructure.payment.toss.dto.TossErrorResponse;
import roomescape.infrastructure.payment.toss.dto.TossPaymentResponse;

/**
 * PaymentGateway 포트의 Toss 구현(어댑터). Toss 의 요청·응답·에러 포맷은 이 클래스 밖으로 새어 나가지 않는다(부패 방지 계층).
 */
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
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw TossPaymentException.of(
                            res.getStatusCode(),
                            objectMapper.readValue(res.getBody(), TossErrorResponse.class)
                    );
                })
                .body(TossPaymentResponse.class);

        return new PaymentResult(
                response.paymentKey(),
                response.orderId(),
                PaymentStatus.from(response.status()),
                response.totalAmount()
        );
    }

}
