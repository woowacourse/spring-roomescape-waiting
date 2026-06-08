package roomescape.reservation.domain;

import roomescape.common.exception.DomainException;

import java.util.Arrays;

import static roomescape.reservation.exception.ReservationErrorCode.INVALID_STATUS;

public enum Status {
    WAITING, CONFIRMED, CANCELED;


    public static Status from(String status) {
        return Arrays.stream(Status.values())
                .filter(s -> s.toString().equals(status))
                .findFirst()
                .orElseThrow(() -> new DomainException(INVALID_STATUS));
    }
}
