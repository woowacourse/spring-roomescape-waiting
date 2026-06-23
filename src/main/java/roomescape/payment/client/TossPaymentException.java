package roomescape.payment.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import roomescape.payment.PaymentFailureCategory;
import roomescape.payment.PaymentGatewayException;
import roomescape.payment.client.dto.TossErrorResponse;

public class TossPaymentException extends PaymentGatewayException {

    private final HttpStatusCode status;

    public TossPaymentException(HttpStatusCode status, PaymentFailureCategory failureCategory, String code,
                                String message) {
        super(failureCategory, code, message);
        this.status = status;
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
            default -> new TossPaymentException(status, PaymentFailureCategory.UNKNOWN, error.code(), error.message());
        };
    }

    public HttpStatusCode getStatus() {
        return status;
    }

    public static class AlreadyProcessed extends TossPaymentException {

        public AlreadyProcessed(String message) {
            super(HttpStatus.BAD_REQUEST, PaymentFailureCategory.UNKNOWN, "ALREADY_PROCESSED_PAYMENT", message);
        }
    }

    public static class DuplicatedOrder extends TossPaymentException {

        public DuplicatedOrder(String message) {
            super(HttpStatus.BAD_REQUEST, PaymentFailureCategory.UNKNOWN, "DUPLICATED_ORDER_ID", message);
        }
    }

    public static class SessionExpired extends TossPaymentException {

        public SessionExpired(String message) {
            super(HttpStatus.BAD_REQUEST, PaymentFailureCategory.DEFINITIVE, "NOT_FOUND_PAYMENT_SESSION", message);
        }
    }

    public static class InvalidRequest extends TossPaymentException {

        public InvalidRequest(String message) {
            super(HttpStatus.BAD_REQUEST, PaymentFailureCategory.CONFIGURATION, "INVALID_REQUEST", message);
        }
    }

    public static class GatewayConfig extends TossPaymentException {

        public GatewayConfig(String code, String message) {
            super(HttpStatus.UNAUTHORIZED, PaymentFailureCategory.CONFIGURATION, code, message);
        }
    }

    public static class CardRejected extends TossPaymentException {

        public CardRejected(String message) {
            super(HttpStatus.FORBIDDEN, PaymentFailureCategory.DEFINITIVE, "REJECT_CARD_PAYMENT", message);
        }
    }

    public static class PaymentNotFound extends TossPaymentException {

        public PaymentNotFound(String message) {
            super(HttpStatus.NOT_FOUND, PaymentFailureCategory.DEFINITIVE, "NOT_FOUND_PAYMENT", message);
        }
    }

    public static class Retryable extends TossPaymentException {

        public Retryable(String message) {
            super(HttpStatus.INTERNAL_SERVER_ERROR, PaymentFailureCategory.UNKNOWN,
                    "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", message);
        }
    }
}
