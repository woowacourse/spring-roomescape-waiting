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

    public static ReservationResponse from(final Reservation reservation) {
        return new ReservationResponse(
                reservation.id(),
                UserResponse.from(reservation.user()),
                reservation.slot().date(),
                TimeSlotResponse.from(reservation.slot().timeSlot()),
                ThemeResponse.from(reservation.slot().theme())
        );
    }

    public static List<ReservationResponse> from(final List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
