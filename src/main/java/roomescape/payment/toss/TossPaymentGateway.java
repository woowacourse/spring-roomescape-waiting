package roomescape.payment.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;

/**
 * 토스 결제 승인 어댑터(부패 방지 계층). 토스 요청·응답·에러 *포맷(DTO)*은 이 클래스 밖으로 새지 않는다.
 * RestClient(base-url·인증 헤더)는 {@link TossConfig}가 조립하고, 여기선 경로·바디·에러 변환만 다룬다.
 * 에러는 onStatus에서 TossPaymentException으로 변환되어 호출부로 전파된다.
 */
@Component
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final TossProperties properties;

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper, TossProperties properties) {
        this.restClient = tossRestClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        TossConfirmRequest request = new TossConfirmRequest(
                confirmation.paymentKey(), confirmation.orderId(), confirmation.amount());

        TossPaymentResponse response = restClient.post()
                .uri(properties.confirmUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    HttpStatusCode status = res.getStatusCode();
                    String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    TossErrorResponse error = null;
                    try {
                        error = body.isBlank() ? null : objectMapper.readValue(body, TossErrorResponse.class);
                    } catch (Exception ignored) {
                        // 본문이 JSON이 아니면 아래 기본 예외로 떨어진다(미정의 코드 처리).
                    }
                    if (error == null || error.code() == null) {
                        throw new TossPaymentException(status, "TOSS_ERROR",
                                "토스 결제 승인에 실패했습니다. (status=" + status.value() + ")");
                    }
                    throw TossPaymentException.of(status, error);
                })
                .body(TossPaymentResponse.class);

        if (response == null) {
            throw new TossPaymentException(HttpStatus.INTERNAL_SERVER_ERROR, "EMPTY_RESPONSE",
                    "결제 응답이 비어 있습니다.");
        }
        return new PaymentResult(response.paymentKey(), response.orderId(),
                response.status(), response.totalAmount());
    }

    @Override
    public String clientKey() {
        return properties.clientKey();
    }
}
