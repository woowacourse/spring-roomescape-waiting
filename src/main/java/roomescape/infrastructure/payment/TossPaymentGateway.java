package roomescape.infrastructure.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.SocketTimeoutException;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentResult;
import roomescape.domain.PaymentStatus;
import roomescape.exception.TossPaymentException;
import roomescape.infrastructure.payment.dto.ConfirmRequest;
import roomescape.infrastructure.payment.dto.TossErrorResponse;
import roomescape.infrastructure.payment.dto.TossPaymentResponse;
import roomescape.service.PaymentGateway;

/**
 * PaymentGateway 포트의 Toss 구현(어댑터). Toss 의 요청·응답·에러 포맷은 이 클래스 밖으로 새어 나가지 않는다(부패 방지 계층).
 */
@Component
@Primary
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper) {
        this.tossRestClient = tossRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        var request = new ConfirmRequest(
                confirmation.paymentKey(), confirmation.orderId(), confirmation.amount());
        try {
            var response = tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", confirmation.orderId())
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        try (var bodyStream = res.getBody()) {
                            var error = objectMapper.readValue(bodyStream, TossErrorResponse.class);
                            throw TossPaymentException.of(res.getStatusCode(), error);
                        } catch (IOException e) {
                            // 본문을 읽을 수 없거나 파싱에 실패한 경우, 상태코드 기반으로 기본 예외를 던진다.
                            throw TossPaymentException.of(res.getStatusCode(),
                                    new TossErrorResponse("UNKNOWN", "알 수 없는 에러가 발생했습니다."));
                        }
                    })
                    .body(TossPaymentResponse.class);
            return toResult(response);
        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException && e.getMessage() != null && e.getMessage()
                    .contains("Read timed out")) {
                throw new TossPaymentException.ReadTimeout("토스 결제 승인 응답이 지연되고 있습니다. 결제 승인 여부가 불분명합니다.", e);
            }
            throw new TossPaymentException.ConnectionFailed("토스 결제 서버와 연결할 수 없습니다.", e);
        } catch (RestClientException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                throw new TossPaymentException.ReadTimeout("토스 결제 승인 응답이 지연되고 있습니다. 결제 승인 여부가 불분명합니다.", e);
            }
            throw e;
        }
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
