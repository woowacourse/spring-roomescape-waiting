package roomescape.payment.gateway.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.payment.PaymentConnectionException;
import roomescape.payment.PaymentResultUnknownException;
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

        TossPaymentResponse response;
        try {
            response = tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    // 주문당 고정 멱등키 — 같은 키로 재호출하면 토스가 첫 응답을 그대로 반환해 이중 승인을 막는다
                    .header("Idempotency-Key", confirmation.idempotencyKey())
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
        } catch (RestClientException e) {
            // 타임아웃·연결 실패는 RestClient가 unchecked로 감싸 던진다. root cause로 연결/읽기 단계를 구분한다.
            // (onStatus가 던지는 TossPaymentException은 RestClientException이 아니므로 여기 걸리지 않는다)
            throw translateTransportFailure(e);
        }

        return new PaymentResult(
                response.paymentKey(),
                response.orderId(),
                PaymentStatus.from(response.status()),
                response.totalAmount()
        );
    }

    private RuntimeException translateTransportFailure(RestClientException e) {
        Throwable cause = NestedExceptionUtils.getMostSpecificCause(e);
        if (cause instanceof SocketTimeoutException) {
            // read timeout("Read timed out")은 승인 여부 불명확 → 확인 필요
            // connect timeout("Connect timed out")은 연결조차 못 함 → 안 됨(재시도 안전)
            if (isReadTimeout(cause)) {
                return new PaymentResultUnknownException("결제 응답을 받지 못했습니다. 결제 결과를 확인해 주세요.", e);
            }
            return new PaymentConnectionException("결제 서버 연결이 시간 내에 이루어지지 않았습니다.", e);
        }
        if (cause instanceof ConnectException) {
            return new PaymentConnectionException("결제 서버에 연결할 수 없습니다.", e);
        }
        // 그 밖의 전송 오류도 연결 실패로 본다(승인 여부가 확실치 않다면 확인 필요로 두는 게 안전하나,
        // 여기까지 오는 케이스는 대개 연결 단계 문제이므로 재시도 안전한 연결 실패로 처리)
        return new PaymentConnectionException("결제 요청을 전송하지 못했습니다.", e);
    }

    private boolean isReadTimeout(Throwable cause) {
        String message = cause.getMessage();
        return message != null && message.toLowerCase().contains("read timed out");
    }
}
