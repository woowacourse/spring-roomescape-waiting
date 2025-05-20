package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.entity.Reservation;
import roomescape.entity.Waiting;
import roomescape.global.ReservationStatus;

public record MyReservationResponse(
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static MyReservationResponse from(Reservation reservation) {

        return new MyReservationResponse(
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getStartAt(),
                ReservationStatus.RESERVED.getText()
        );
    }

    public static MyReservationResponse from(Waiting waiting) {

        return new MyReservationResponse(
                waiting.getThemeName(),
                waiting.getDate(),
                waiting.getStartAt(),
                ReservationStatus.WAIT.getText()
        );
    }
}
