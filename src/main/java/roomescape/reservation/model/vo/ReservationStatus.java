package roomescape.reservation.model.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.reservation.model.entity.ReservationTime;

public enum ReservationStatus {
    CONFIRMED,
    ENDED;

    public static ReservationStatus getStatus(LocalDateTime reservationDateTime, LocalDateTime now) {
        if(now.isAfter(reservationDateTime)) {
            return ENDED;
        }
        return CONFIRMED;

    }
}
