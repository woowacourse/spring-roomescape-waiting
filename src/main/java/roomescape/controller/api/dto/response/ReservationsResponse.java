package roomescape.controller.api.dto.response;

import roomescape.service.dto.output.ReservationOutput;

import java.util.List;

public record ReservationsResponse(List<ReservationResponse> data) {

    public static ReservationsResponse toResponse(final List<ReservationOutput> outputs) {
        return new ReservationsResponse(
                outputs.stream()
                        .map(ReservationResponse::toResponse)
                        .toList()
        );
    }
}
