package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

public record ReservationWaitingResponse(long id, String themeName, LocalDate date, LocalTime time, String status) {

    public static ReservationWaitingResponse from(Reservation reservation) {
        return new ReservationWaitingResponse(reservation.getId(), reservation.getTheme().getName(),
                reservation.getDate(), reservation.getReservationTime().getStartAt(),
                ReservationStatus.RESERVED.getStatus());
    }
}
