package roomescape.reservation.repository.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public record ReservationWaitingResult(
        Long id,
        String guestName,
        LocalDate date,
        ReservationTime time,
        Theme theme,
        Status status,
        long waitNumber
) {
    public static ReservationWaitingResult from(Reservation reservation, long waitNumber) {
        return new ReservationWaitingResult(
                reservation.getId(),
                reservation.getGuestName(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                reservation.getStatus(),
                waitNumber
        );
    }
}
