package roomescape.presentation.response;

import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.waiting.Waiting;

public record UserReservationResponse(
        long reservationId,
        ThemeResponse theme,
        LocalDate date,
        TimeSlotResponse time,
        ReservationStatus status

) {

    public static UserReservationResponse fromReservation(final Reservation reservation) {
        return new UserReservationResponse(
                reservation.id(),
                ThemeResponse.from(reservation.theme()),
                reservation.date(),
                TimeSlotResponse.from(reservation.timeSlot()),
                ReservationStatus.RESERVED
        );
    }

    public static UserReservationResponse fromWaiting(final Waiting waiting) {
        return new UserReservationResponse(
                waiting.id(),
                ThemeResponse.from(waiting.theme()),
                waiting.date(),
                TimeSlotResponse.from(waiting.timeSlot()),
                ReservationStatus.WAITING
        );
    }
}
