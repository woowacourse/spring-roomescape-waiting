package roomescape.service.result;

import java.time.LocalTime;

public record AvailableReservationTimeResult(Long timeId, LocalTime startAt, boolean booked) {
}
