package roomescape.controller.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;

public record ReservationWaitingResponse(
        long id,
        LocalDate date,
        String themeName,
        String themeDescription,
        String themeThumbnailUrl,
        LocalTime time,
        long waitingNumber
) {
    public static ReservationWaitingResponse from(Reservation reservation, long waitingNumber) {
        return new ReservationWaitingResponse(
                reservation.getId(),
                reservation.getDate(),
                reservation.getTheme().getName(),
                reservation.getTheme().getDescription(),
                reservation.getTheme().getThumbnailUrl(),
                reservation.getTime().getStartAt(),
                waitingNumber
        );
    }
}
