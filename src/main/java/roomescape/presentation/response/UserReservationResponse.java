package roomescape.presentation.response;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;

public record UserReservationResponse(
        long reservationId,
        ThemeResponse theme,
        LocalDate date,
        TimeSlotResponse time,
        ReservationStatus status

) {

    public static UserReservationResponse from(final Reservation reservation) {
        return new UserReservationResponse(
                reservation.id(),
                ThemeResponse.from(reservation.slot().theme()),
                reservation.slot().date(),
                TimeSlotResponse.from(reservation.slot().timeSlot()),
                reservation.status()
        );
    }

    public static List<UserReservationResponse> from(final List<Reservation> reservations) {
        return reservations.stream()
                .map(UserReservationResponse::from)
                .toList();
    }
}
