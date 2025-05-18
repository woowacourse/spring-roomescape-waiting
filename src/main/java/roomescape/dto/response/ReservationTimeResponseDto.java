package roomescape.dto.response;

import java.time.LocalTime;
import roomescape.model.ReservationTime;

public record ReservationTimeResponseDto(
        Long id,
        LocalTime startAt
) {
    public ReservationTimeResponseDto(ReservationTime reservationTime) {
        this(
                reservationTime.getId(),
                reservationTime.getStartAt()
        );
    }
}
