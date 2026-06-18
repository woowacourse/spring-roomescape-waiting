package roomescape.domain.reservation.dto.response;

import java.time.LocalTime;

public record ReservationTimeStartAtResponseDto(Long reservationId, LocalTime startAt) {

}
