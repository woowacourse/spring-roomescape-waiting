package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.Reservation;

public record ReservationResponse(
        Long id,
        String memberName,
        LocalDate date,
        ReservationTimeResponse time,
        String themeName
) {
    public static ReservationResponse toDto(Reservation reservation) {
        ReservationTimeResponse dto = ReservationTimeResponse.toDto(reservation.getReservationTime());
        return new ReservationResponse(reservation.getId(), reservation.getMember().getName(), reservation.getDate(),
                dto,
                reservation.getTheme().getName());
    }
}
