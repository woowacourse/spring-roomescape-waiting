package roomescape.domain;

import java.util.Locale;

public enum PaymentStatus {
    DONE,
    CANCELED,
    PARTIAL_CANCELED,
    ABORTED,
    EXPIRED,
    READY,
    IN_PROGRESS,
    WAITING_FOR_DEPOSIT,
    UNKNOWN;

    public static PaymentStatus from(String status) {
        if (status == null || status.isBlank()) {
            return UNKNOWN;
        }

        try {
            return PaymentStatus.valueOf(status.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
