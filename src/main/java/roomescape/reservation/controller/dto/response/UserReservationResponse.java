package roomescape.reservation.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.application.dto.response.UserReservationServiceResponse;
import roomescape.reservation.model.vo.ReservationStatus;

public record UserReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime time,
        ReservationStatus status
) {

    public static UserReservationResponse from(UserReservationServiceResponse response) {
        return new UserReservationResponse(
                response.reservationId(),
                response.themeName(),
                response.date(),
                response.time(),
                response.status()
        );
    }
}
