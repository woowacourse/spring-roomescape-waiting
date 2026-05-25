package roomescape.time.controller.dto;

import java.time.LocalTime;
import roomescape.time.exception.InvalidTimeRequestValueException;
import roomescape.time.service.ReservationTimeCommand;

public record ReservationTimeRequest(LocalTime startAt) {

    public ReservationTimeRequest {
        if (startAt == null) {
            throw new InvalidTimeRequestValueException();
        }
    }

    public ReservationTimeCommand toCommand() {
        return new ReservationTimeCommand(startAt);
    }
}
