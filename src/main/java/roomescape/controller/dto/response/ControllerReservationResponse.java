package roomescape.controller.dto.response;

import java.time.LocalDate;
import roomescape.service.dto.response.ServiceReservationResponse;

public record ControllerReservationResponse(Long id, String name, LocalDate date,
                                            ControllerReservationTimeResponse time,
                                            ControllerThemeResponse theme) {
    public static ControllerReservationResponse from(ServiceReservationResponse response) {
        return new ControllerReservationResponse(
                response.id(),
                response.name(),
                response.date(),
                ControllerReservationTimeResponse.from(response.time()),
                ControllerThemeResponse.from(response.theme()));
    }
}
