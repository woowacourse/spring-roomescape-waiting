package roomescape.domain;

import roomescape.exception.ExceptionType;
import roomescape.exception.RoomescapeException;

public enum ReservationStatus {
    APPROVED,
    PENDING;

    public String makeStatusMessage(ReservationStatus reservationStatus, Long rank) {
        if (reservationStatus == APPROVED) {
            return "예약";
        }
        if (reservationStatus == PENDING) {
            return rank + "번째 예약대기";
        }
        throw new RoomescapeException(ExceptionType.NOT_FOUND_RESERVATION_STATUS);
    }
}
