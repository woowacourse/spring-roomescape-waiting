package roomescape.domain;

import roomescape.exception.ExceptionType;
import roomescape.exception.RoomescapeException;

public enum ReservationStatus {
    APPROVED,
    PENDING;

    private static final String APPROVED_MESSAGE = "예약";
    private static final String PENDING_MESSAGE = "번째 예약대기";

    public String makeStatusMessage(Long rank) {
        if (this == APPROVED) {
            return APPROVED_MESSAGE;
        }
        if (this == PENDING) {
            return rank + PENDING_MESSAGE;
        }
        throw new RoomescapeException(ExceptionType.NOT_FOUND_RESERVATION_STATUS);
    }
}
