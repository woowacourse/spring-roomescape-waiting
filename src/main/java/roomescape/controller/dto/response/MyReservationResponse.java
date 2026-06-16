package roomescape.controller.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.MyReservation;

public record MyReservationResponse(
        long id,
        LocalDate date,
        String themeName,
        String themeDescription,
        String themeThumbnailUrl,
        LocalTime time,
        String status,
        Long waitingNumber
) {
    public static MyReservationResponse from(MyReservation myReservation) {
        var reservation = myReservation.reservation();
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                reservation.getTheme().getName(),
                reservation.getTheme().getDescription(),
                reservation.getTheme().getThumbnailUrl(),
                reservation.getTime().getStartAt(),
                myReservation.isWaiting() ? "예약 대기" : "예약",
                myReservation.waitingNumber()
        );
    }
}
