package roomescape.reservation.domain;

import static roomescape.reservation.exception.ReservationErrorCode.INVALID_RESERVATION_STATUS;

import java.util.Arrays;
import roomescape.common.exception.DomainException;

public enum Status {
    WAITING,
    CONFIRMED,
    CANCELED;

    public static Status from(String status) {
        return Arrays.stream(Status.values())
                .filter(s -> s.toString().equals(status))
                .findFirst()
                .orElseThrow(() -> new DomainException(INVALID_RESERVATION_STATUS));
    }

    public boolean isConfirmed() {
        return this == CONFIRMED;
    }
}
