package roomescape.reservation.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.application.dto.response.ReservationServiceResponse;

public record ReservationResponse(
        Long id,
        String name,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        String themeName
) {

    public static ReservationResponse from(ReservationServiceResponse response) {
        return new ReservationResponse(
                response.id(),
                response.name(),
                response.date(),
                response.startAt(),
                response.themeName()
        );
    }
}
