package roomescape.controller.client.api.dto.response;

import java.time.LocalTime;

public record TimeSlotProjection(
        Long id,
        LocalTime startAt,
        Boolean isReservable
) {
}
