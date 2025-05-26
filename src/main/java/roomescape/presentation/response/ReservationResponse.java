package roomescape.presentation.response;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.reservation.Reservation;

public record ReservationResponse(
        long id,
        UserResponse user,
        LocalDate date,
        TimeSlotResponse time,
        ThemeResponse theme
) {

    public static List<ReservationResponse> fromReservations(
            final List<Reservation> reservations
    ) {
        return reservations.stream()
                .map(ReservationResponse::fromReservation)
                .toList();
    }

    public static ReservationResponse fromReservation(
            final Reservation reservation
    ) {
        return new ReservationResponse(
                reservation.id(),
                UserResponse.fromUser(reservation.user()),
                reservation.date(),
                TimeSlotResponse.fromTimeSlot(reservation.timeSlot()),
                ThemeResponse.fromTheme(reservation.theme())
        );
    }
}
