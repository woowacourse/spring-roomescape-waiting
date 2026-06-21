package roomescape.infrastructure.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.exception.code.PaymentErrorCode;
import roomescape.exception.domain.PaymentException;
import roomescape.infrastructure.payment.client.dto.TossConfirmRequest;
import roomescape.infrastructure.payment.client.dto.TossConfirmResponse;
import roomescape.infrastructure.payment.client.dto.TossErrorResponse;

@Component
public class TossPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(TossPaymentGateway.class);
    private static final String CONFIRM_PATH = "/v1/payments/confirm";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper) {
        this.restClient = tossRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        TossConfirmRequest request = new TossConfirmRequest(
                confirmation.paymentKey(),
                confirmation.orderId(),
                confirmation.amount()
        );
        try {
            return executeConfirm(request, confirmation);
        } catch (ResourceAccessException e) {
            handleConnectionException(e, confirmation.orderId());
            throw e; // unreachable — handleConnectionException always throws
        } catch (RestClientException e) {
            handleRestClientException(e, confirmation.orderId());
            throw e; // 읽기 타임아웃이 아닌 경우 원래 예외를 그대로 전파
        }
    }

    private PaymentResult executeConfirm(TossConfirmRequest request, PaymentConfirmation confirmation) {
        TossConfirmResponse response = restClient.post()
                .uri(CONFIRM_PATH)
                .header("Idempotency-Key", confirmation.orderId())
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                .body(TossConfirmResponse.class);

        log.info("Toss 결제 승인 성공: orderId={}, paymentKey={}", confirmation.orderId(), response.paymentKey());
        return new PaymentResult(response.paymentKey());
    }

    private void handleErrorResponse(HttpRequest request, ClientHttpResponse response) throws IOException {
        TossErrorResponse error = parseErrorBody(response.getBody().readAllBytes());
        log.warn("Toss 결제 승인 실패: httpStatus={}, code={}, message={}",
                response.getStatusCode(), error.code(), error.message());
        throw toPaymentException(error);
    }

    private void handleConnectionException(ResourceAccessException resourceAccessException, String orderId) {
        Throwable cause = resourceAccessException.getCause();
        if (cause instanceof ConnectException) {
            throwConnectFailed("연결 거부", orderId, resourceAccessException);
        }
        if (cause instanceof SocketTimeoutException socketEx) {
            handleSocketTimeoutException(socketEx, orderId, resourceAccessException);
        }
        throwConnectFailed("I/O 오류", orderId, resourceAccessException);
    }

    private void handleSocketTimeoutException(SocketTimeoutException socketEx, String orderId, Exception e) {
        if (isConnectPhase(socketEx)) {
            throwConnectFailed("연결 타임아웃", orderId, e);
        }
        log.warn("Toss 결제 승인 read timeout (결과 불명확): orderId={}", orderId, e);
        throw new PaymentException(PaymentErrorCode.PAYMENT_READ_TIMEOUT, e);
    }

    private void throwConnectFailed(String reason, String orderId, Exception e) {
        log.error("Toss 결제 승인 {}: orderId={}", reason, orderId, e);
        throw new PaymentException(PaymentErrorCode.PAYMENT_CONNECT_FAILED);
    }

    private void handleRestClientException(RestClientException e, String orderId) {
        if (!isReadTimeout(e)) {
            return;
        }
        log.warn("Toss 결제 승인 read timeout (결과 불명확): orderId={}", orderId, e);
        throw new PaymentException(PaymentErrorCode.PAYMENT_READ_TIMEOUT, e);
    }

    private boolean isReadTimeout(RestClientException e) {
        SocketTimeoutException socketEx = findSocketTimeoutInCauseChain(e);
        if (socketEx == null) {
            return false;
        }
        return !isConnectPhase(socketEx);
    }

    private SocketTimeoutException findSocketTimeoutInCauseChain(Throwable t) {
        for (Throwable cause = t.getCause(); cause != null; cause = cause.getCause()) {
            if (cause instanceof SocketTimeoutException socketEx) {
                return socketEx;
            }
        }
        return null;
    }

    private boolean isConnectPhase(SocketTimeoutException socketEx) {
        return getMessageLowerCase(socketEx).contains("connect");
    }

    private String getMessageLowerCase(Throwable t) {
        String message = t.getMessage();
        if (message == null) {
            return "";
        }
        return message.toLowerCase();
    }

    private TossErrorResponse parseErrorBody(byte[] body) {
        try {
            return objectMapper.readValue(body, TossErrorResponse.class);
        } catch (IOException e) {
            log.warn("Toss 에러 응답 파싱 실패", e);
            return new TossErrorResponse("UNKNOWN", "결제 오류 응답을 파싱할 수 없습니다.");
        }
    }

    private PaymentException toPaymentException(TossErrorResponse error) {
        PaymentErrorCode errorCode = switch (error.code()) {
            case "ALREADY_PROCESSED_PAYMENT" -> PaymentErrorCode.ALREADY_PROCESSED_PAYMENT;
            case "DUPLICATED_ORDER_ID" -> PaymentErrorCode.DUPLICATED_ORDER_ID;
            case "NOT_FOUND_PAYMENT_SESSION" -> PaymentErrorCode.NOT_FOUND_PAYMENT_SESSION;
            case "INVALID_REQUEST" -> PaymentErrorCode.INVALID_REQUEST;
            case "UNAUTHORIZED_KEY" -> PaymentErrorCode.UNAUTHORIZED_KEY;
            case "INVALID_API_KEY" -> PaymentErrorCode.INVALID_API_KEY;
            case "REJECT_CARD_PAYMENT" -> PaymentErrorCode.REJECT_CARD_PAYMENT;
            case "NOT_FOUND_PAYMENT" -> PaymentErrorCode.NOT_FOUND_PAYMENT;
            case "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING" ->
                    PaymentErrorCode.FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING;
            default -> {
                log.warn("정의되지 않은 Toss 에러 코드: code={}, message={}", error.code(), error.message());
                yield PaymentErrorCode.PAYMENT_GATEWAY_ERROR;
            }
        };
        return new PaymentException(errorCode);
    }
}
