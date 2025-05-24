package roomescape.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record WaitingReservationResponse(Long id, String name, String theme, LocalDate date, LocalTime startAt) {
    public static WaitingReservationResponse from(Reservation reservation) {
        return new WaitingReservationResponse(reservation.getId(), reservation.name(), reservation.themeName(),
                reservation.getDate(), reservation.reservationTime());
    }
}
