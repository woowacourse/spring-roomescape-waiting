package roomescape.pg.client;

import lombok.Getter;
import roomescape.pg.client.dto.TossErrorResponse;

@Getter
public class TossPaymentException extends RuntimeException {

    private final String code;

    public TossPaymentException(String code, String message) {
        super(message);
        this.code = code;
    }

    public static TossPaymentException of(TossErrorResponse error) {
        return switch (error.code()) {
            case "ALREADY_PROCESSED_PAYMENT" -> new AlreadyProcessed(error.message());
            case "DUPLICATED_ORDER_ID" -> new DuplicatedOrder(error.message());
            case "NOT_FOUND_PAYMENT_SESSION" -> new SessionExpired(error.message());
            case "INVALID_REQUEST" -> new InvalidRequest(error.message());
            case "UNAUTHORIZED_KEY", "INVALID_API_KEY" -> new GatewayConfig(error.message());
            case "REJECT_CARD_PAYMENT" -> new CardRejected(error.message());
            case "NOT_FOUND_PAYMENT" -> new PaymentNotFound(error.message());
            case "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING" -> new Retryable(error.message());
            default -> new TossPaymentException(error.code(), error.message());
        };
    }

    public static class AlreadyProcessed extends TossPaymentException {

        public AlreadyProcessed(String message) {
            super("ALREADY_PROCESSED_PAYMENT", message);
        }

    }

    public static class DuplicatedOrder extends TossPaymentException {

        public DuplicatedOrder(String message) {
            super("DUPLICATED_ORDER_ID", message);
        }

    }

    public static class SessionExpired extends TossPaymentException {

        public SessionExpired(String message) {
            super("NOT_FOUND_PAYMENT_SESSION", message);
        }

    }

    public static class InvalidRequest extends TossPaymentException {

        public InvalidRequest(String message) {
            super("INVALID_REQUEST", message);
        }

    }

    public static class GatewayConfig extends TossPaymentException {

        public GatewayConfig(String message) {
            super("UNAUTHORIZED_KEY", message);
        }

    }

    public static class CardRejected extends TossPaymentException {

        public CardRejected(String message) {
            super("REJECT_CARD_PAYMENT", message);
        }

    }

    public static class PaymentNotFound extends TossPaymentException {

        public PaymentNotFound(String message) {
            super("NOT_FOUND_PAYMENT", message);
        }

    }

    public static class Retryable extends TossPaymentException {

        public Retryable(String message) {
            super("FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", message);
        }

    }

}
