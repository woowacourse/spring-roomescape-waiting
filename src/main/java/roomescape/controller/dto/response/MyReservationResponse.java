package roomescape.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.MyReservation;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationResponse(
        long id,
        LocalDate date,
        String themeName,
        String themeDescription,
        String themeThumbnailUrl,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        Long waitingNumber,
        ReservationStatus reservationStatus
) {
    public static MyReservationResponse from(MyReservation myReservation) {
        Reservation reservation = myReservation.reservation();
        Theme theme = reservation.getTheme();

        return new MyReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl(),
                reservation.getTime().getStartAt(),
                myReservation.waitingNumber(),
                ReservationStatus.valueOf(myReservation.reservationType().name())
        );
    }
}
