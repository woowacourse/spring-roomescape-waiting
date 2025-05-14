package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.Reservation;

public record ReservationWithStatusResponse(
        Long id,
        String memberName,
        LocalDate date,
        ReservationTimeResponse time,
        String themeName,
        String status
) {
    public static ReservationWithStatusResponse toDto(Reservation reservation) {
        ReservationTimeResponse dto = ReservationTimeResponse.toDto(reservation.getReservationTime());
        return new ReservationWithStatusResponse(reservation.getId(), reservation.getMember().getName(),
                reservation.getDate(),
                dto,
                reservation.getTheme().getName(),
                "예약"
        );
    }
}
