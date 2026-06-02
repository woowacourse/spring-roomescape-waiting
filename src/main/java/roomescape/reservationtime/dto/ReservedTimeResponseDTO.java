package roomescape.reservationtime.dto;

import java.time.LocalTime;
import roomescape.reservationtime.domain.ReservedTime;

public record ReservedTimeResponseDTO(
        Long timeId,
        LocalTime startAt,
        boolean reserved
) {
    public static ReservedTimeResponseDTO from(ReservedTime reservedTime) {
        return new ReservedTimeResponseDTO(
                reservedTime.time().getId(),
                reservedTime.time().getStartAt(),
                reservedTime.reserved()
        );
    }
}
