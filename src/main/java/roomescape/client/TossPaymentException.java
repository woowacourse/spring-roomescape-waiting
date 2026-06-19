package roomescape.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import roomescape.client.dto.TossErrorResponse;

public class TossPaymentException extends RuntimeException {
    private final HttpStatusCode status;
    private final String code;

    public TossPaymentException(HttpStatusCode status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static TossPaymentException of(HttpStatusCode status, TossErrorResponse error) {
        return switch (error.code()) {
            case "ALREADY_PROCESSED_PAYMENT" -> new AlreadyProcessed(error.message());
            case "DUPLICATED_ORDER_ID" -> new InvalidRequest(error.message(), "DUPLICATED_ORDER_ID");
            case "NOT_FOUND_PAYMENT_SESSION" -> new InvalidRequest(error.message(), "NOT_FOUND_PAYMENT_SESSION");
            case "INVALID_REQUEST" -> new InvalidRequest(error.message(), "INVALID_REQUEST");
            case "UNAUTHORIZED_KEY", "INVALID_API_KEY" -> new GatewayConfig(error.message(), error.code());
            case "REJECT_CARD_PAYMENT" -> new CardRejected(error.message());
            case "NOT_FOUND_PAYMENT" -> new PaymentNotFound(error.message());
            case "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING" -> new Retryable(error.message());
            default -> new TossPaymentException(status, error.code(), error.message());
        };
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

    public static class InvalidRequest extends TossPaymentException {
        public InvalidRequest(String message, String code) {
            super(HttpStatus.BAD_REQUEST, code, message);
        }
    }

    public static class GatewayConfig extends TossPaymentException {
        public GatewayConfig(String message, String code) {
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
}
