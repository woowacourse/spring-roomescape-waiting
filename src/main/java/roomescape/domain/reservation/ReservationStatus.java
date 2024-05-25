package roomescape.domain.reservation;

import roomescape.exception.NotFoundException;

public enum ReservationStatus {

    CONFIRMED,
    WAITING,
    CANCELED,
    REJECTED;

    public static ReservationStatus fromString(String status) {
        for (ReservationStatus reservationStatus : ReservationStatus.values()) {
            if (reservationStatus.name().equalsIgnoreCase(status)) {
                return reservationStatus;
            }
        }
        throw new NotFoundException("일치하는 예약 상태가 존재하지 않아 변환할 수 없습니다.");
    }
}
