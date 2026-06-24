package roomescape.payment.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.ConnectException;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import roomescape.payment.PaymentConnectionException;
import roomescape.payment.PaymentUncertainException;
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

        try {
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
        } catch (ResourceAccessException e) {
            // ConnectException: TCP 연결 자체 실패 → Toss에 요청이 도달하지 않았으므로 취소 안전
            if (e.getCause() instanceof ConnectException) {
                throw new PaymentConnectionException("결제 서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.");
            }
            // SocketTimeoutException: read timeout → Toss가 이미 처리했을 수 있어 결과 불명
            throw new PaymentUncertainException("결제 처리 결과를 확인할 수 없습니다. 내 예약에서 결제 상태를 확인해 주세요.");
        }
    }
}
