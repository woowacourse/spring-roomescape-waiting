package roomescape.controller.api.dto.response;

import roomescape.service.dto.output.AvailableReservationTimeOutput;

public record AvailReservationTimeResponse(long timeId, String startAt, boolean alreadyBooked) {
    public static AvailReservationTimeResponse toResponse(final AvailableReservationTimeOutput output) {
        return new AvailReservationTimeResponse(output.timeId(), output.startAt(), output.alreadyBooked());
    }
}
