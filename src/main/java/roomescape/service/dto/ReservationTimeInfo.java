package roomescape.service.dto;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;
import roomescape.repository.dto.ReservationTimeDto;

public record ReservationTimeInfo(
        Long id,
        LocalTime startAt
) {
    public static ReservationTimeInfo from(ReservationTimeDto reservationTimeDto) {
        return new ReservationTimeInfo(
                reservationTimeDto.id(),
                reservationTimeDto.startAt()
        );
    }

    public static ReservationTimeInfo from(ReservationTime reservationTime) {
        return new ReservationTimeInfo(
                reservationTime.getId(),
                reservationTime.getStartAt()
        );
    }

    public ReservationTime toEntity() {
        return new ReservationTime(id, startAt);
    }
}
