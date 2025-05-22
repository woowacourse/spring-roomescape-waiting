package roomescape.reservation.model.vo;

import java.time.LocalDateTime;

public enum ReservationStatus {
    CONFIRMED,
    WAITING,
    ENDED;

    public static ReservationStatus determineStatus(LocalDateTime reservationDateTime, LocalDateTime now) {
        if (now.isAfter(reservationDateTime)) {
            return ENDED;
        }
        return CONFIRMED;
    }
}
