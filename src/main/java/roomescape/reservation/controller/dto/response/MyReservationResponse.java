package roomescape.reservation.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.application.dto.response.MyReservationServiceResponse;
import roomescape.reservation.model.vo.ReservationStatus;

public record MyReservationResponse(
    Long reservationId,
    String theme,
    LocalDate date,
    @JsonFormat(pattern = "HH:mm") LocalTime time,
    ReservationStatus status
) {

    public static MyReservationResponse from(MyReservationServiceResponse response) {
        return new MyReservationResponse(
            response.reservationId(),
            response.themeName(),
            response.date(),
            response.time(),
            response.status()
        );
    }
}
