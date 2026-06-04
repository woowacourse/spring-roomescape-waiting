package roomescape.controller.dto.response;

import java.util.List;
import roomescape.service.dto.response.ServiceReceptionListResponse;

public record ControllerReceptionListResponse(
        List<ControllerReservationResponse> reservations,
        List<ControllerWaitResponse> waits
) {
    public static ControllerReceptionListResponse from(ServiceReceptionListResponse response) {
        List<ControllerReservationResponse> reservationResponses = response.reservations().stream()
                .map(ControllerReservationResponse::from)
                .toList();

        List<ControllerWaitResponse> waitResponses = response.waits().stream()
                .map(ControllerWaitResponse::from)
                .toList();

        return new ControllerReceptionListResponse(reservationResponses, waitResponses);
    }
}
