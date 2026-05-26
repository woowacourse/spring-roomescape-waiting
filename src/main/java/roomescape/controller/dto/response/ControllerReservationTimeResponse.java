package roomescape.controller.dto.response;

import java.time.LocalTime;
import roomescape.service.dto.response.ServiceReservationTimeResponse;

public record ControllerReservationTimeResponse(Long id, LocalTime startAt) {
    public static ControllerReservationTimeResponse from(ServiceReservationTimeResponse response) {
        return new ControllerReservationTimeResponse(
                response.id(),
                response.startAt()
        );
    }
}
