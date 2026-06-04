package roomescape.controller.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.ReservationStatus;
import roomescape.service.dto.response.ServiceWaitResponse;

public record ControllerWaitResponse(
        Long id,
        String name,
        LocalDate date,
        ControllerReservationTimeResponse time,
        ControllerThemeResponse theme,
        ReservationStatus status,
        Long order,
        LocalDateTime createdAt
) implements ControllerReceptionResponse {

    public static ControllerWaitResponse from(ServiceWaitResponse response) {
        return new ControllerWaitResponse(
                response.id(),
                response.name(),
                response.date(),
                ControllerReservationTimeResponse.from(response.time()),
                ControllerThemeResponse.from(response.theme()),
                response.status(),
                response.order(),
                response.createdAt()
        );
    }
}
