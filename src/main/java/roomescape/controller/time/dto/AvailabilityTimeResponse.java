package roomescape.controller.time.dto;

import java.time.format.DateTimeFormatter;
import roomescape.domain.ReservationTime;

public record AvailabilityTimeResponse(Long id, String startAt, boolean booked) {

    public static AvailabilityTimeResponse from(final ReservationTime time, final boolean booked) {
        return new AvailabilityTimeResponse(
                time.getId(),
                time.getStartAt().format(DateTimeFormatter.ofPattern("HH:mm")),
                booked
        );
    }
}
