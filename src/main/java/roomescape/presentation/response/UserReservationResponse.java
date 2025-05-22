package roomescape.presentation.response;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.reservation.Reservation;

public record UserReservationResponse(
    long reservationId,
    LocalDate date,
    TimeSlotResponse time,
    ThemeResponse theme,
    String status
) {

    public static UserReservationResponse from(final Reservation reservation) {
        return new UserReservationResponse(
            reservation.id(),
            reservation.slot().date(),
            TimeSlotResponse.from(reservation.slot().timeSlot()),
            ThemeResponse.from(reservation.slot().theme()),
            reservation.status().description()
        );
    }

    public static List<UserReservationResponse> from(final List<Reservation> reservations) {
        return reservations.stream()
            .map(UserReservationResponse::from)
            .toList();
    }
}
