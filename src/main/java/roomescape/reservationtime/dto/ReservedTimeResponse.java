package roomescape.reservationtime.dto;

import java.time.LocalTime;
import roomescape.reservationtime.domain.ReservedTime;

public record ReservedTimeResponse(
        Long timeId,
        LocalTime startAt,
        boolean reserved
) {
    public static ReservedTimeResponse from(ReservedTime reservedTime) {
        return new ReservedTimeResponse(
                reservedTime.time().getId(),
                reservedTime.time().getStartAt(),
                reservedTime.reserved()
        );
    }
}
