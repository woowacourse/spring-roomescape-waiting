package roomescape.time.controller.response;

import java.time.LocalTime;

public record AvailableReservationTimeResponse(Long id, LocalTime startAt, boolean isReserved) {
}
