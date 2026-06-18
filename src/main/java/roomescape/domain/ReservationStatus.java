package roomescape.domain;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;


public enum ReservationStatus {
    RESERVED,
    PAYMENT_WAITING,
    WAITING;

    public static ReservationStatus from(String status) {
        return switch (status) {
            case "RESERVED" -> RESERVED;
            case "WAITING" -> WAITING;
            default -> throw new RoomescapeException(ErrorType.INVALID_DOMAIN,
                    "올바르지 않은 예약 상태 이름입니다. 변환 희망 상태 값: " + status);
        };
    }
}
