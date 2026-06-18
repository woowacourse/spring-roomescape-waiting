package roomescape.payment.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
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
 * 에러 응답은 {@link #translateErrorStatus}가, 전송 실패는 {@link #translateTransportFailure}가 도메인 의미로 번역한다.
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
                    .onStatus(HttpStatusCode::isError, this::translateErrorStatus)
                    .body(TossPaymentResponse.class);
        } catch (RestClientException e) {
            throw translateTransportFailure(e);
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
     * 토스 에러 응답(4xx/5xx)을 코드별 TossPaymentException으로 번역한다(onStatus 핸들러).
     * 본문이 비었거나 JSON이 아니면 미정의 코드로 보고 기본 예외로 떨어진다.
     */
    private void translateErrorStatus(HttpRequest request, ClientHttpResponse response) throws IOException {
        HttpStatusCode status = response.getStatusCode();
        String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
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
    }

    /**
     * 전송 단계 실패를 도메인 의미로 번역한다(인프라 예외가 서비스로 새지 않게). wrapper 타입으론 못 가른다
     * — read timeout도 connect 실패도 ResourceAccessException으로 온다. 그래서 root cause를 본다:
     * 연결 '거부'(ConnectException)만 '확실히 안 됨'이 확실하고, 그 외는 결과 불명확이라 '모름'으로 떨어뜨린다(안전 기본값).
     */
    private RuntimeException translateTransportFailure(RestClientException e) {
        if (isConnectionRefused(e)) {
            return new PaymentGatewayUnreachableException("결제 서버에 연결하지 못했습니다.", e);
        }
        return new PaymentResultUnknownException("결제 결과를 확인하지 못했습니다.", e);
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
