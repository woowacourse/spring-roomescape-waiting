package roomescape.controller.client.api.dto.response;

import java.time.LocalTime;

public record TimeSlotResponse(
        Long id,
        LocalTime startAt,
        Boolean isReservable
) {
}
