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
        TimeSlotResponse dto = TimeSlotResponse.from(reservation.getTimeSlot());
        return new ReservationResponse(reservation.getId(), reservation.getMember().getName(), reservation.getDate(),
                dto,
                reservation.getTheme().getName());
    }
}
