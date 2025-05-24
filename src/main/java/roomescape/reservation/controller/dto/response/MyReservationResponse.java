package roomescape.reservation.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.application.dto.response.MyBookingServiceResponse;

public record MyReservationResponse(
    Long reservationId,
    String theme,
    LocalDate date,
    @JsonFormat(pattern = "HH:mm") LocalTime time,
    String status
) {

    public static MyReservationResponse from(MyBookingServiceResponse response) {
        return new MyReservationResponse(
            response.reservationId(),
            response.themeName(),
            response.date(),
            response.time(),
            response.status()
        );
    }
}
