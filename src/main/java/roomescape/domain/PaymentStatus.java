package roomescape.domain;

public enum PaymentStatus {

    READY,
    IN_PROGRESS,
    WAITING_FOR_DEPOSIT,
    DONE,
    CANCELED,
    PARTIAL_CANCELED,
    ABORTED,
    EXPIRED,
    UNKNOWN,
    NO_RESPONSE,
    ;

    public static PaymentStatus from(String raw) {
        if (raw == null) {
            return UNKNOWN;
        }

        try {
            return valueOf(raw);
        } catch (IllegalArgumentException exception) {
            return UNKNOWN;
        }
    }
}
