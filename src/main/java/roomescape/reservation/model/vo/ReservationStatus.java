package roomescape.reservation.model.vo;

import java.time.LocalDateTime;

public enum ReservationStatus {
    CONFIRMED,
    ENDED;

    public static ReservationStatus getStatus(LocalDateTime reservationDateTime, LocalDateTime now) {
        if (now.isAfter(reservationDateTime)) {
            return ENDED;
        }
        return CONFIRMED;
    }
}
