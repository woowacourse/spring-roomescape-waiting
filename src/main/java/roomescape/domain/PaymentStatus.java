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
    EXPIRED;

    public static PaymentStatus from(String value) {
        return Arrays.stream(values())
                .filter(status -> status.name().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 결제 상태입니다. value=" + value));
    }
}
