package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;

public record ReservationResponse(
        Long id,
        String memberName,
        LocalDate date,
        TimeSlotResponse time,
        String themeName
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getDate(),
                TimeSlotResponse.from(reservation.getTimeSlot()),
                reservation.getTheme().getName()
        );
    }
}
