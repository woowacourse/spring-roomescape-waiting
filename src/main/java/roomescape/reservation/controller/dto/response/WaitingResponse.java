package roomescape.reservation.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.application.dto.response.WaitingServiceResponse;

public record WaitingResponse(
    Long id,
    String name,
    LocalDate date,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime startAt,
    String themeName
) {

    public static WaitingResponse from(WaitingServiceResponse response) {
        return new WaitingResponse(
            response.id(),
            response.name(),
            response.date(),
            response.startAt(),
            response.themeName()
        );
    }
}
