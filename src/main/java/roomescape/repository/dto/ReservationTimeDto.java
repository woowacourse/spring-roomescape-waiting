package roomescape.repository.dto;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record ReservationTimeDto(
        Long id,
        LocalTime startAt
) {
    public static ReservationTimeDto from(ReservationTime time) {
        return new ReservationTimeDto(
                time.getId(),
                time.getStartAt()
        );
    }
}
