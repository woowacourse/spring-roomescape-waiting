package roomescape.repository.dto;

import java.time.LocalTime;

public record TimeSlotProjection(
        Long id,
        LocalTime startAt,
        Boolean isReservable
) {
}
