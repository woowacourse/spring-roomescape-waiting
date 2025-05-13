package roomescape.presentation.dto.response;

import java.time.LocalTime;

public record AvailableReservationTimeResponse(Long id, LocalTime startAt, boolean isReserved) {
}
