package roomescape.controller.dto.response;

import roomescape.service.dto.response.ServiceReceptionResponse;
import roomescape.service.dto.response.ServiceReservationResponse;
import roomescape.service.dto.response.ServiceWaitResponse;

public sealed interface ControllerReceptionResponse permits ControllerReservationResponse, ControllerWaitResponse {

    static ControllerReceptionResponse from(ServiceReceptionResponse response) {
        return switch (response) {
            case ServiceReservationResponse reservation -> ControllerReservationResponse.from(reservation);
            case ServiceWaitResponse wait -> ControllerWaitResponse.from(wait);
        };
    }
}
