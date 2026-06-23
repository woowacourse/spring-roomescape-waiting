package roomescape.domain;

import java.util.Arrays;

public enum PaymentStatus {
    READY,
    IN_PROGRESS,
    WAITING_FOR_DEPOSIT,
    DONE,
    CANCELED,
    PARTIAL_CANCELED,
    ABORTED,
    EXPIRED,
    UNKNOWN;

    public static PaymentStatus from(String rawStatus) {
        return Arrays.stream(values())
                .filter(status -> status.name().equals(rawStatus))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
