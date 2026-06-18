package roomescape.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.payment.PaymentStatus;
import roomescape.global.exception.RateLimitException;
import roomescape.payment.client.dto.ConfirmRequest;
import roomescape.payment.client.dto.TossErrorResponse;
import roomescape.payment.client.dto.TossPaymentResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.time.Duration;

/**
 * PaymentGateway 포트의 Toss 구현(어댑터). Toss 의 요청·응답·에러 포맷은 이 클래스 밖으로 새어 나가지 않는다(부패 방지 계층).
 */
@Component
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;
    private final Bucket bucket;

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper) {
        this.tossRestClient = tossRestClient;
        this.objectMapper = objectMapper;
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillGreedy(10, Duration.ofSeconds(1))
                .build();
        this.bucket = Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        if (!bucket.tryConsume(1)) {
            throw new RateLimitException("외부 결제 API 호출 한도를 초과했습니다. 잠시 후 다시 시도해 주세요.");
        }
        var request = new ConfirmRequest(
                confirmation.paymentKey(), confirmation.orderId(), confirmation.amount());
        var response = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", confirmation.orderId())
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    var error = objectMapper.readValue(res.getBody(), TossErrorResponse.class);
                    throw TossPaymentException.of(res.getStatusCode(), error);
                })
                .body(TossPaymentResponse.class);
        assert response != null;
        return toResult(response);
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
