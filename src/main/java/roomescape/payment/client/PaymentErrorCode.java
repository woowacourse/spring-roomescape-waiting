package roomescape.payment.client;

public enum PaymentErrorCode {

    ALREADY_PROCESSED,
    DUPLICATED_ORDER,
    SESSION_EXPIRED,
    INVALID_REQUEST,
    GATEWAY_CONFIG,
    CARD_REJECTED,
    PAYMENT_NOT_FOUND,
    RETRYABLE,
    UNKNOWN;

    public static PaymentErrorCode fromTossCode(String tossCode) {
        if (tossCode == null) {
            return UNKNOWN;
        }
        return switch (tossCode) {
            case "ALREADY_PROCESSED_PAYMENT" -> ALREADY_PROCESSED;
            case "DUPLICATED_ORDER_ID" -> DUPLICATED_ORDER;
            case "NOT_FOUND_PAYMENT_SESSION" -> SESSION_EXPIRED;
            case "INVALID_REQUEST" -> INVALID_REQUEST;
            case "UNAUTHORIZED_KEY", "INVALID_API_KEY" -> GATEWAY_CONFIG;
            case "REJECT_CARD_PAYMENT" -> CARD_REJECTED;
            case "NOT_FOUND_PAYMENT" -> PAYMENT_NOT_FOUND;
            case "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING" -> RETRYABLE;
            default -> UNKNOWN;
        };
    }
}