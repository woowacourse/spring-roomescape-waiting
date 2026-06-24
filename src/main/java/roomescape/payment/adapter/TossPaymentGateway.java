package roomescape.payment.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import roomescape.payment.TossPaymentException;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.dto.ConfirmRequest;
import roomescape.payment.port.PaymentGateway;

public class TossPaymentGateway implements PaymentGateway {

    // 외부 API 서버에 HTTP를 보내는 도구
    private final RestClient tossRestClient;
    // JSON / JAVA 객체 변환기
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper) {
        this.tossRestClient = tossRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        // ConfirmRequest 로 /v1/payments/confirm 을 호출하고, 에러 응답은 onStatus 에서 TossPaymentException.of(...) 로
        // 성공 응답은 PaymentResult 로 변환해 반환한다.
        ConfirmRequest request = new ConfirmRequest(confirmation.paymentKey(), confirmation.orderId(),
                confirmation.amount());

        TossPaymentResponse tossResponse = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .header("Idempotency-Key", confirmation.orderId())
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    TossErrorResponse error = objectMapper.readValue(res.getBody(), TossErrorResponse.class);
                    throw TossPaymentException.of(res.getStatusCode(), error);
                })
                .body(TossPaymentResponse.class);

        return new PaymentResult(tossResponse.paymentKey());
    }
}
