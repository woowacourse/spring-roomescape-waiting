package roomescape.time.repository.projection;

import java.time.LocalTime;

public record AvailableSlotTime(
        Long slotId,
        Long timeId,
        LocalTime startAt,
        boolean isActive
) {
}
