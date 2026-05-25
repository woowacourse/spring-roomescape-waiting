package roomescape.time.controller.dto;

import java.time.LocalTime;
import roomescape.time.exception.InvalidTimeRequestFormatException;
import roomescape.time.service.dto.ReservationTimeCommand;

public record ReservationTimeRequest(LocalTime startAt) {

    public ReservationTimeRequest {
        if (startAt == null) {
            throw new InvalidTimeRequestFormatException();
        }
    }

    public ReservationTimeCommand toCommand() {
        return new ReservationTimeCommand(startAt);
    }
}
