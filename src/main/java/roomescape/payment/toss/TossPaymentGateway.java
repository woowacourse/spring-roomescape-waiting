package roomescape.payment.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentGatewayUnreachableException;
import roomescape.payment.PaymentResult;
import roomescape.payment.PaymentResultUnknownException;

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

        TossPaymentResponse response;
        try {
            response = restClient.post()
                    .uri(properties.confirmUrl())
                    .header("Idempotency-Key", confirmation.idempotencyKey())
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
        } catch (RestClientException e) {
            // 전송 단계 실패를 도메인 의미로 번역한다(인프라 예외가 서비스로 새지 않게 여기서).
            // wrapper 타입으론 못 가른다 — read timeout도 connect 실패도 둘 다 ResourceAccessException으로 온다.
            // 그래서 root cause를 본다: 연결 '거부'(ConnectException)만 '요청이 닿지 못함=확실히 안 됨'이 확실하고,
            // 그 외(SocketTimeout 등)는 요청이 갔을 수 있어 결과 불명확 → '모름'으로 떨어뜨린다(안전 기본값).
            // TossPaymentException은 RestClientException이 아니므로 여기 안 걸리고 그대로 전파된다.
            if (isConnectionRefused(e)) {
                throw new PaymentGatewayUnreachableException("결제 서버에 연결하지 못했습니다.", e);
            }
            throw new PaymentResultUnknownException("결제 결과를 확인하지 못했습니다.", e);
        }

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

    /**
     * 전송 실패의 근본 원인이 '연결 거부'(ConnectException)인지 본다. 연결이 거부됐다면 요청이
     * 토스에 닿지조차 못한 게 확실하다 — 이때만 '확실히 안 됨'으로 단정할 수 있다.
     */
    private boolean isConnectionRefused(Throwable e) {
        for (Throwable cause = e; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConnectException) {
                return true;
            }
        }
        return false;
    }
}
