package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;

public record ReservationWithStatusResponse(
        Long id,
        String memberName,
        LocalDate date,
        TimeSlotResponse time,
        String themeName
) {
    public static ReservationWithStatusResponse from(Reservation reservation) {
        return new ReservationWithStatusResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getDate(),
                TimeSlotResponse.from(reservation.getTimeSlot()),
                reservation.getTheme().getName()
        );
    }
}
