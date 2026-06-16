package roomescape.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.payment.client.dto.TossPaymentResponse;
import roomescape.payment.exception.TossErrorResponse;
import roomescape.payment.exception.TossPaymentException;
import roomescape.payment.service.dto.PaymentConfirmation;
import roomescape.payment.service.dto.PaymentResult;
import roomescape.payment.service.dto.PaymentStatus;

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
        TossPaymentResponse tossPaymentResponse = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .body(confirmation)
                .retrieve()
                .onStatus(HttpStatusCode::isError, ((request, response) -> {
                    var error = objectMapper.readValue(response.getBody(), TossErrorResponse.class);
                    throw TossPaymentException.of(response.getStatusCode(), error);
                }))
                .body(TossPaymentResponse.class);

        return new PaymentResult(
                tossPaymentResponse.paymentKey(),
                tossPaymentResponse.orderId(),
                PaymentStatus.valueOf(tossPaymentResponse.status()),
                tossPaymentResponse.totalAmount()
        );
    }

}
