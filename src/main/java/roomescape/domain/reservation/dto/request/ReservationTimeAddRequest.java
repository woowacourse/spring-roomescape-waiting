package roomescape.domain.reservation.dto.request;

import java.time.LocalTime;

public record ReservationTimeAddRequest(LocalTime startAt) {
}
