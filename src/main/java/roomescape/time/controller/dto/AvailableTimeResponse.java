package roomescape.time.controller.dto;

import java.time.LocalTime;
import roomescape.time.repository.dto.AvailableTimeQueryResult;

public record AvailableTimeResponse(Long id, LocalTime startAt, boolean alreadyBooked) {

    public static AvailableTimeResponse from(AvailableTimeQueryResult time) {
        return new AvailableTimeResponse(time.id(), time.startAt(), time.alreadyBooked());
    }
}
