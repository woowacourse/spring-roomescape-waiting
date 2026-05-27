package roomescape.service.dto.command;

import java.time.LocalTime;
import roomescape.controller.dto.request.ReservationTimeRequest;

public record ReservationTimeCommand(
        LocalTime startAt
) {
    public static ReservationTimeCommand from(ReservationTimeRequest request) {
        return new ReservationTimeCommand(request.startAt());
    }
}