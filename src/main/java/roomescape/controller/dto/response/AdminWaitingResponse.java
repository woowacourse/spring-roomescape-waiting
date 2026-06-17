package roomescape.controller.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.MyReservation;

public record AdminWaitingResponse(
        long id,
        String name,
        LocalDate date,
        String themeName,
        String themeDescription,
        String themeThumbnailUrl,
        LocalTime time,
        long waitingNumber
) {
    public static AdminWaitingResponse from(MyReservation myReservation) {
        var reservation = myReservation.reservation();
        return new AdminWaitingResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                reservation.getTheme().getName(),
                reservation.getTheme().getDescription(),
                reservation.getTheme().getThumbnailUrl(),
                reservation.getTime().getStartAt(),
                myReservation.waitingNumber()
        );
    }
}
