package roomescape.business.dto;

import java.util.List;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.StartTime;

public record ReservationTimeDto(
        Id id,
        StartTime startTime
) {
    public static ReservationTimeDto fromEntity(final ReservationTime time) {
        return new ReservationTimeDto(
                time.getId(),
                time.getStartTime()
        );
    }

    public static List<ReservationTimeDto> fromEntities(final List<ReservationTime> reservationTimes) {
        return reservationTimes.stream()
                .map(ReservationTimeDto::fromEntity)
                .toList();
    }
}
