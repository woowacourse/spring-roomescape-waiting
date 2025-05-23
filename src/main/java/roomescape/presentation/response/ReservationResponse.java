package roomescape.presentation.response;

import java.time.LocalDate;
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
                reservation.date(),
                TimeSlotResponse.from(reservation.timeSlot()),
                ThemeResponse.from(reservation.theme())
        );
    }
}
