package roomescape.payment.infra.toss;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import roomescape.payment.domain.exception.PaymentAlreadyProcessedException;
import roomescape.payment.domain.exception.PaymentConfirmationPendingException;
import roomescape.payment.domain.exception.PaymentGatewayException;
import roomescape.payment.domain.exception.PaymentInvalidRequestException;
import roomescape.payment.domain.exception.PaymentKeyConfigurationException;
import roomescape.payment.domain.exception.PaymentNotFoundException;
import roomescape.payment.domain.exception.PaymentRejectedException;
import roomescape.payment.domain.exception.PaymentRetryableException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Locale;

final class TossPaymentExceptionMapper {
    private static final String DEFAULT_MESSAGE = "결제 승인에 실패했습니다.";
    private static final String CONNECTION_UNAVAILABLE_MESSAGE =
            "결제 승인 서버에 연결하지 못했습니다. 결제 내역에서 결과를 확인한 뒤 다시 시도해주세요.";
    private static final String TIMEOUT_MESSAGE =
            "결제 승인 요청에 응답이 없습니다. 승인 여부가 확인되지 않았습니다. 결제 내역에서 결과를 확인한 뒤 다시 시도해주세요.";

    private TossPaymentExceptionMapper() {
    }

    static RuntimeException map(HttpStatusCode statusCode, TossErrorResponse errorResponse) {
        String code = code(errorResponse);
        String message = message(errorResponse);

        return switch (code) {
            case "ALREADY_PROCESSED_PAYMENT" -> new PaymentAlreadyProcessedException("이미 승인된 결제입니다.");
            case "DUPLICATED_ORDER_ID", "NOT_FOUND_PAYMENT_SESSION", "INVALID_REQUEST" ->
                    new PaymentInvalidRequestException(message);
            case "UNAUTHORIZED_KEY", "INVALID_API_KEY" ->
                    new PaymentKeyConfigurationException("Toss Payments 키 설정을 확인해주세요.");
            case "REJECT_CARD_PAYMENT" -> new PaymentRejectedException(message);
            case "NOT_FOUND_PAYMENT" -> new PaymentNotFoundException("결제 정보를 찾을 수 없습니다.");
            case "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING" -> new PaymentRetryableException(
                    "결제사 내부 처리 지연으로 승인에 실패했습니다. 잠시 후 다시 시도해주세요."
            );
            default -> new PaymentGatewayException(DEFAULT_MESSAGE + " (" + statusCode.value() + ")");
        };
    }

    static RuntimeException map(RestClientException exception) {
        if (hasCause(exception, ConnectException.class)) {
            return new PaymentConfirmationPendingException(CONNECTION_UNAVAILABLE_MESSAGE, exception);
        }
        if (exception instanceof ResourceAccessException && hasConnectTimeoutCause(exception)) {
            return new PaymentConfirmationPendingException(CONNECTION_UNAVAILABLE_MESSAGE, exception);
        }
        if (hasCause(exception, SocketTimeoutException.class)) {
            return pendingForTimeout(exception);
        }

        return exception;
    }

    static PaymentConfirmationPendingException pendingForTimeout(Throwable exception) {
        return new PaymentConfirmationPendingException(TIMEOUT_MESSAGE, exception);
    }

    private static String code(TossErrorResponse errorResponse) {
        if (errorResponse == null || errorResponse.code() == null || errorResponse.code().isBlank()) {
            return "UNKNOWN";
        }
        return errorResponse.code();
    }

    private static String message(TossErrorResponse errorResponse) {
        if (errorResponse == null || errorResponse.message() == null || errorResponse.message().isBlank()) {
            return DEFAULT_MESSAGE;
        }
        return errorResponse.message();
    }

    private static boolean hasCause(Throwable exception, Class<? extends Throwable> causeType) {
        Throwable current = exception;
        while (current != null) {
            if (causeType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static boolean hasConnectTimeoutCause(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof SocketTimeoutException && isConnectTimeoutMessage(current.getMessage())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static boolean isConnectTimeoutMessage(String message) {
        return message != null && message.toLowerCase(Locale.ROOT).contains("connect");
    }
}
