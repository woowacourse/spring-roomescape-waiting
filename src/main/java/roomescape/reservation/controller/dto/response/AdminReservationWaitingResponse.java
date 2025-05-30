package roomescape.reservation.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.application.dto.response.AdminReservationWaitingServiceResponse;

public record AdminReservationWaitingResponse(
        Long id,
        String waitingName,
        String themeName,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt
) {

    public static AdminReservationWaitingResponse from(AdminReservationWaitingServiceResponse response) {
        return new AdminReservationWaitingResponse(
                response.id(),
                response.waitingName(),
                response.themeName(),
                response.date(),
                response.startAt()
        );
    }
}
