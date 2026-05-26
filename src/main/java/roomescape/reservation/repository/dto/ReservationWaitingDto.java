package roomescape.reservation.repository.dto;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;

public record ReservationWaitingDto(
        Long id,
        String guestName,
        LocalDate date,
        ReservationTime time,
        Theme theme,
        Status status,
        long waitNumber
) {
    public static ReservationWaitingDto from(Reservation reservation, long waitNumber) {
        return new ReservationWaitingDto(
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
