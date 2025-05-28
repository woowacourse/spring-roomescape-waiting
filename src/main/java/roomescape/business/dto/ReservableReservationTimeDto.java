package roomescape.business.dto;

import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.StartTime;

public record ReservableReservationTimeDto(
        Id id,
        StartTime startTime,
        boolean available
) {
    public static ReservableReservationTimeDto fromEntity(final ReservationTime time, final boolean available) {
        return new ReservableReservationTimeDto(
                time.getId(),
                time.getStartTime(),
                available
        );
    }
}
