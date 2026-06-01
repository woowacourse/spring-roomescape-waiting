package roomescape.controller.dto.response;

import java.time.LocalDate;
import roomescape.domain.ReservationStatus;
import roomescape.service.dto.response.ServiceReceptionResponse;

public record ControllerReservationResponse(
        Long id,
        String name,
        LocalDate date,
        ControllerReservationTimeResponse time,
        ControllerThemeResponse theme,
        ReservationStatus status,
        Long order
) {
    public static ControllerReservationResponse from(ServiceReceptionResponse response) {
        return new ControllerReservationResponse(
                response.id(),
                response.name(),
                response.reservationDate(),
                ControllerReservationTimeResponse.from(response.time()),
                ControllerThemeResponse.from(response.theme()),
                response.status(),
                response.order()
        );
    }
}
