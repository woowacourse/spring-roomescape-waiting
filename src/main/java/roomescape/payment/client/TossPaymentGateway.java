package roomescape.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
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
    @Retryable(
            exceptionExpression = "#root.retryable",
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2) // TODO 기준 공부 후 적용
    )
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        System.out.println(confirmation.toString());
        try {
            TossPaymentResponse tossResponse = tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", confirmation.orderId())
                    .body(confirmation)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        var error = objectMapper.readValue(response.getBody(), TossErrorResponse.class);
                        throw TossPaymentException.of(response.getStatusCode(), error);
                    })
                    .body(TossPaymentResponse.class);

            return new PaymentResult(
                    tossResponse.paymentKey(),
                    tossResponse.orderId(),
                    PaymentStatus.from(tossResponse.status()),
                    tossResponse.totalAmount()
            );
        } catch (RestClientException e) {
            throw new TossPaymentException.Retryable(e.getMessage());
        }
    }

}
