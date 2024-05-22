package roomescape.service.dto;

import roomescape.domain.ReservationStatus;

import java.util.stream.Stream;

import static roomescape.domain.ReservationStatus.RESERVED;
import static roomescape.domain.ReservationStatus.WAITING;

public enum ReservationStatusMessageMapper {
    RESERVED_MESSAGE(RESERVED, "예약"),
    WAITING_MESSAGE(WAITING, "예약 대기"),
    ;

    private final ReservationStatus status;
    private final String message;

    ReservationStatusMessageMapper(ReservationStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public static String messageOf(ReservationStatus target, int rank) {
        if (target == RESERVED) {
            return RESERVED_MESSAGE.message;
        }
        return rank + "번째 " + WAITING_MESSAGE.message;
    }
}
