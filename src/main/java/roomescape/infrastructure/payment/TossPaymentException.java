package roomescape.infrastructure.payment;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClientException;
import roomescape.exception.RoomescapeBaseException;

public class TossPaymentException extends RoomescapeBaseException {

    private final HttpStatusCode status;
    private final String code;

    public TossPaymentException(HttpStatusCode status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public TossPaymentException(HttpStatusCode status, String code, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.code = code;
    }

    public static TossPaymentException of(HttpStatusCode status, TossErrorResponse error) {
        return switch (error.code()) {
            case "ALREADY_PROCESSED_PAYMENT" -> new AlreadyProcessed(error.message());
            case "DUPLICATED_ORDER_ID" -> new DuplicatedOrder(error.message());
            case "NOT_FOUND_PAYMENT_SESSION" -> new SessionExpired(error.message());
            case "INVALID_REQUEST" -> new InvalidRequest(error.message());
            case "UNAUTHORIZED_KEY", "INVALID_API_KEY" -> new GatewayConfig(error.code(), error.message());
            case "REJECT_CARD_PAYMENT" -> new CardRejected(error.message());
            case "NOT_FOUND_PAYMENT" -> new PaymentNotFound(error.message());
            case "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING" -> new Retryable(error.message());
            default -> new TossPaymentException(status, error.code(), error.message());
        };
    }

    public static TossPaymentException fromNetworkFailure(RestClientException exception) {
        Throwable rootCause = rootCause(exception);
        if (rootCause instanceof ConnectException || isConnectTimeout(rootCause)) {
            return new ConnectionFailed(exception);
        }
        if (rootCause instanceof SocketTimeoutException) {
            return new ConfirmationUnknown(exception);
        }
        return new NetworkFailure(exception);
    }

    private static Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private static boolean isConnectTimeout(Throwable throwable) {
        if (!(throwable instanceof SocketTimeoutException)) {
            return false;
        }
        String message = throwable.getMessage();
        return message != null && message.toLowerCase(Locale.ROOT).contains("connect");
    }

    public HttpStatusCode getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public static class AlreadyProcessed extends TossPaymentException {

        public AlreadyProcessed(String message) {
            super(HttpStatus.BAD_REQUEST, "ALREADY_PROCESSED_PAYMENT", message);
        }
    }

    public static class DuplicatedOrder extends TossPaymentException {

        public DuplicatedOrder(String message) {
            super(HttpStatus.BAD_REQUEST, "DUPLICATED_ORDER_ID", message);
        }
    }

    public static class SessionExpired extends TossPaymentException {

        public SessionExpired(String message) {
            super(HttpStatus.BAD_REQUEST, "NOT_FOUND_PAYMENT_SESSION", message);
        }
    }

    public static class InvalidRequest extends TossPaymentException {

        public InvalidRequest(String message) {
            super(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message);
        }
    }

    public static class GatewayConfig extends TossPaymentException {

        public GatewayConfig(String code, String message) {
            super(HttpStatus.UNAUTHORIZED, code, message);
        }
    }

    public static class CardRejected extends TossPaymentException {

        public CardRejected(String message) {
            super(HttpStatus.FORBIDDEN, "REJECT_CARD_PAYMENT", message);
        }
    }

    public static class PaymentNotFound extends TossPaymentException {

        public PaymentNotFound(String message) {
            super(HttpStatus.NOT_FOUND, "NOT_FOUND_PAYMENT", message);
        }
    }

    public static class Retryable extends TossPaymentException {

        public Retryable(String message) {
            super(HttpStatus.INTERNAL_SERVER_ERROR, "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", message);
        }
    }

    public static class ConnectionFailed extends TossPaymentException {

        public ConnectionFailed(Throwable cause) {
            super(HttpStatus.SERVICE_UNAVAILABLE, "TOSS_CONNECTION_FAILED",
                    "결제 승인 요청을 보낼 수 없습니다. 잠시 후 다시 시도해주세요.", cause);
        }
    }

    public static class ConfirmationUnknown extends TossPaymentException {

        public ConfirmationUnknown(Throwable cause) {
            super(HttpStatus.GATEWAY_TIMEOUT, "TOSS_CONFIRMATION_UNKNOWN",
                    "결제 승인 응답을 받지 못했습니다. 결제가 완료됐는지 확인한 뒤 다시 시도해주세요.", cause);
        }
    }

    public static class NetworkFailure extends TossPaymentException {

        public NetworkFailure(Throwable cause) {
            super(HttpStatus.SERVICE_UNAVAILABLE, "TOSS_NETWORK_FAILED",
                    "결제 승인 요청 중 네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", cause);
        }
    }
}
