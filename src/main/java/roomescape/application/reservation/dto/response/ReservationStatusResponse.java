package roomescape.application.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;

public record ReservationStatusResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static ReservationStatusResponse from(Reservation reservation) {
        Theme theme = reservation.getTheme();
        ReservationTime time = reservation.getTime();
        return new ReservationStatusResponse(
                reservation.getId(),
                theme.getName(),
                reservation.getDate(),
                time.getStartAt(),
                "예약"
        );
    }
}
