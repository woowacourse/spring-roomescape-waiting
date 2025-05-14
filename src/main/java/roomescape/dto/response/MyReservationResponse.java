package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.entity.Reservation;

public record MyReservationResponse(Long reservationId,
                                    String theme,
                                    LocalDate date,
                                    LocalTime time,
                                    String status) {

    public static MyReservationResponse from(Reservation reservation) {

        return new MyReservationResponse(
                reservation.getId(),
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getStartAt(),
                reservation.getStatus().getText());
    }
}
