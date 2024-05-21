package roomescape.controller.api.dto.response;

import roomescape.service.dto.output.ReservationTimeOutput;

public record ReservationTimeResponse(long id, String startAt) {

    public static ReservationTimeResponse toResponse(final ReservationTimeOutput output) {
        return new ReservationTimeResponse(output.id(), output.startAt());
    }
}
