package roomescape.payment.gateway.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.domain.PaymentStatus;
import roomescape.payment.gateway.PaymentGateway;
import roomescape.payment.gateway.toss.dto.TossErrorResponse;
import roomescape.payment.gateway.toss.dto.TossPaymentResponse;

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
        record ConfirmRequest(String paymentKey, String orderId, Long amount) {}

        TossPaymentResponse response = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ConfirmRequest(confirmation.paymentKey(), confirmation.orderId(), confirmation.amount()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    HttpStatusCode statusCode = res.getStatusCode();
                    try {
                        TossErrorResponse error = objectMapper.readValue(res.getBody(), TossErrorResponse.class);
                        throw TossPaymentException.of(statusCode, error);
                    } catch (java.io.IOException e) {
                        // ponytail: HttpURLConnection consumes 401 body on auth-retry — fall back to status-code mapping
                        throw switch (statusCode.value()) {
                            case 401 -> new TossPaymentException.GatewayConfig("인증 실패");
                            case 403 -> new TossPaymentException.CardRejected("카드 거절");
                            case 404 -> new TossPaymentException.PaymentNotFound("결제 정보 없음");
                            case 500 -> new TossPaymentException.Retryable("Toss 내부 오류");
                            default  -> new TossPaymentException(statusCode, "UNKNOWN_ERROR", e.getMessage());
                        };
                    }
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
