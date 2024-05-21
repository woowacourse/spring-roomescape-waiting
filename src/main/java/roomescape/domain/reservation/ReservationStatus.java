package roomescape.domain.reservation;

import roomescape.exception.NotFoundException;

public enum ReservationStatus {

    CONFIRMED,
    WAITING;

    public static ReservationStatus getReservationStatus(String status) {
        if (CONFIRMED.name().equals(status)) {
            return CONFIRMED;
        }
        if (WAITING.name().equals(status)) {
            return WAITING;
        }
        throw new NotFoundException("예약 상태가 존재하지 않습니다.");
    }
}
