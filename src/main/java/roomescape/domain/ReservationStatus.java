package roomescape.domain;

import roomescape.exception.InvalidDomainException;

public enum ReservationStatus {
    RESERVED,
    WAITING;

    public static ReservationStatus from(String status) {
        return switch (status) {
            case "RESERVED" -> RESERVED;
            case "WAITING" -> WAITING;
            default -> throw new InvalidDomainException("올바르지 않은 예약 상태 이름입니다. 변환 희망 상태 값: " + status);
        };
    }
}
