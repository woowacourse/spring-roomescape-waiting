package roomescape.payment.client;

import org.springframework.http.HttpStatus;
import roomescape.exception.business.BusinessException;
import roomescape.payment.client.dto.TossErrorResponse;

public class TossPaymentException extends BusinessException {

    private final PaymentErrorCode errorCode;

    public TossPaymentException(HttpStatus status, PaymentErrorCode errorCode, String message) {
        super(status, message);
        this.errorCode = errorCode;
    }

    public static TossPaymentException of(HttpStatus status, TossErrorResponse error) {
        return switch (error.code()) {
            case "ALREADY_PROCESSED_PAYMENT" -> new AlreadyProcessed(error.message());
            case "DUPLICATED_ORDER_ID" -> new DuplicatedOrder(error.message());
            case "NOT_FOUND_PAYMENT_SESSION" -> new SessionExpired(error.message());
            case "INVALID_REQUEST" -> new InvalidRequest(error.message());
            case "UNAUTHORIZED_KEY", "INVALID_API_KEY" -> new GatewayConfig(error.message());
            case "REJECT_CARD_PAYMENT" -> new CardRejected(error.message());
            case "NOT_FOUND_PAYMENT" -> new PaymentNotFound(error.message());
            case "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING" -> new Retryable(error.message());
            default -> new TossPaymentException(status, PaymentErrorCode.UNKNOWN, error.message());
        };
    }

    public PaymentErrorCode getErrorCode() {
        return errorCode;
    }

    public static class AlreadyProcessed extends TossPaymentException {

        public AlreadyProcessed(String message) {
            super(HttpStatus.BAD_REQUEST, PaymentErrorCode.ALREADY_PROCESSED, message);
        }
    }

    public static class DuplicatedOrder extends TossPaymentException {

        public DuplicatedOrder(String message) {
            super(HttpStatus.BAD_REQUEST, PaymentErrorCode.DUPLICATED_ORDER, message);
        }
    }

    public static class SessionExpired extends TossPaymentException {

        public SessionExpired(String message) {
            super(HttpStatus.BAD_REQUEST, PaymentErrorCode.SESSION_EXPIRED, message);
        }
    }

    public static class InvalidRequest extends TossPaymentException {

        public InvalidRequest(String message) {
            super(HttpStatus.BAD_REQUEST, PaymentErrorCode.INVALID_REQUEST, message);
        }
    }

    public static class GatewayConfig extends TossPaymentException {

        public GatewayConfig(String message) {
            super(HttpStatus.UNAUTHORIZED, PaymentErrorCode.GATEWAY_CONFIG, message);
        }
    }

    public static class CardRejected extends TossPaymentException {

        public CardRejected(String message) {
            super(HttpStatus.FORBIDDEN, PaymentErrorCode.CARD_REJECTED, message);
        }
    }

    public static class PaymentNotFound extends TossPaymentException {

        public PaymentNotFound(String message) {
            super(HttpStatus.NOT_FOUND, PaymentErrorCode.PAYMENT_NOT_FOUND, message);
        }
    }

    public static class Retryable extends TossPaymentException {

        public Retryable(String message) {
            super(HttpStatus.INTERNAL_SERVER_ERROR, PaymentErrorCode.RETRYABLE, message);
        }
    }

    public static class GatewayBusy extends TossPaymentException {

        public GatewayBusy(String message) {
            super(HttpStatus.SERVICE_UNAVAILABLE, PaymentErrorCode.GATEWAY_BUSY, message);
        }
    }
}