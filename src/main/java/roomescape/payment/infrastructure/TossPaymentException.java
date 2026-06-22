package roomescape.payment.infrastructure;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import roomescape.payment.exception.PaymentFailureException;

public class TossPaymentException extends PaymentFailureException {

    protected TossPaymentException(HttpStatus status, String code, String message) {
        super(status, code, message);
    }

    public static TossPaymentException of(HttpStatusCode status, String code, String message) {
        HttpStatus httpStatus = HttpStatus.valueOf(status.value());
        return switch (code) {
            case "ALREADY_PROCESSED_PAYMENT" -> new AlreadyProcessed(httpStatus, code, message);
            case "DUPLICATED_ORDER_ID" -> new DuplicatedOrder(httpStatus, code, message);
            case "NOT_FOUND_PAYMENT_SESSION" -> new SessionExpired(httpStatus, code, message);
            case "INVALID_REQUEST" -> new InvalidRequest(httpStatus, code, message);
            case "UNAUTHORIZED_KEY", "INVALID_API_KEY" -> new GatewayConfig(httpStatus, code, message);
            case "REJECT_CARD_PAYMENT" -> new CardRejected(httpStatus, code, message);
            case "NOT_FOUND_PAYMENT" -> new PaymentNotFound(httpStatus, code, message);
            case "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING" -> new Retryable(httpStatus, code, message);
            default -> new TossPaymentException(httpStatus, code, message);
        };
    }

    public static class AlreadyProcessed extends TossPaymentException {
        public AlreadyProcessed(HttpStatus status, String code, String message) { super(status, code, message); }
    }

    public static class DuplicatedOrder extends TossPaymentException {
        public DuplicatedOrder(HttpStatus status, String code, String message) { super(status, code, message); }
    }

    public static class SessionExpired extends TossPaymentException {
        public SessionExpired(HttpStatus status, String code, String message) { super(status, code, message); }
    }

    public static class InvalidRequest extends TossPaymentException {
        public InvalidRequest(HttpStatus status, String code, String message) { super(status, code, message); }
    }

    public static class GatewayConfig extends TossPaymentException {
        public GatewayConfig(HttpStatus status, String code, String message) { super(status, code, message); }
    }

    public static class CardRejected extends TossPaymentException {
        public CardRejected(HttpStatus status, String code, String message) { super(status, code, message); }
    }

    public static class PaymentNotFound extends TossPaymentException {
        public PaymentNotFound(HttpStatus status, String code, String message) { super(status, code, message); }
    }

    public static class Retryable extends TossPaymentException {
        public Retryable(HttpStatus status, String code, String message) { super(status, code, message); }
    }
}
