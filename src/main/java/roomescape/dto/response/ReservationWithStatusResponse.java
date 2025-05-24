package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.Status;
import roomescape.domain.Waiting;

public record ReservationWithStatusResponse(
        Long id,
        String memberName,
        LocalDate date,
        ReservationTimeResponse time,
        String themeName,
        Status status
) {
    public static ReservationWithStatusResponse from(Reservation reservation) {
        ReservationTimeResponse dto = ReservationTimeResponse.from(reservation.getReservationTime());
        return new ReservationWithStatusResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                reservation.getDate(),
                dto,
                reservation.getTheme().getName(),
                Status.CONFIRMED
        );
    }

    public static ReservationWithStatusResponse from(Waiting waiting) {
        ReservationTimeResponse dto = ReservationTimeResponse.from(waiting.getReservationTime());
        return new ReservationWithStatusResponse(
                waiting.getId(),
                waiting.getMember().getName(),
                waiting.getDate(),
                dto,
                waiting.getTheme().getName(),
                Status.PENDING
        );
    }
}
