package roomescape.controller.api.dto.response;

import roomescape.service.dto.output.ReservationTimeOutput;

import java.util.List;

public record ReservationTimesResponse(List<ReservationTimeResponse> data) {
    public static ReservationTimesResponse from(final List<ReservationTimeOutput> outputs) {
        return new ReservationTimesResponse(
                outputs.stream()
                        .map(ReservationTimeResponse::from)
                        .toList()
        );
    }
}
