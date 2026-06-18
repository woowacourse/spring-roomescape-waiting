package roomescape.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.client.dto.ConfirmRequest;
import roomescape.client.dto.TossErrorResponse;
import roomescape.client.dto.TossPaymentResponse;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.payment.PaymentStatus;

/**
 * PaymentGateway 포트의 Toss 구현(어댑터). Toss 의 요청·응답·에러 포맷은 이 클래스 밖으로 새어 나가지 않는다(부패 방지 계층). 외부 에러 코드 → 도메인
 * 예외(TossPaymentException) 매핑은 TossPaymentException.of 가 맡는다.
 */
@Component
public class TossPaymentGateway implements PaymentGateway {

    private static final String UNKNOWN_CODE = "UNKNOWN";

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper) {
        this.tossRestClient = tossRestClient;
        this.objectMapper = objectMapper;
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
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw TossPaymentException.of(res.getStatusCode(), readError(res));
                })
                .body(TossPaymentResponse.class);
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

    /**
     * 에러 본문을 TossErrorResponse 로 역직렬화한다. 본문이 비었거나 JSON 이 아니어도 원래 실패를 삼키지 않도록 UNKNOWN 코드로 감싼다.
     */
    private TossErrorResponse readError(ClientHttpResponse res) throws IOException {
        byte[] body = res.getBody().readAllBytes();
        if (body.length == 0) {
            return new TossErrorResponse(UNKNOWN_CODE, "토스 에러 응답 본문이 비어 있습니다 (status=" + res.getStatusCode() + ")");
        }
        try {
            return objectMapper.readValue(body, TossErrorResponse.class);
        } catch (IOException e) {
            return new TossErrorResponse(UNKNOWN_CODE,
                    "토스 에러 응답을 해석할 수 없습니다: " + new String(body, StandardCharsets.UTF_8));
        }
    }
}
