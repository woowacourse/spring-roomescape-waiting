package roomescape.business.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    public static List<ReservableReservationTimeDto> fromEntities(final List<ReservationTime> available,
                                                                  final List<ReservationTime> notAvailable) {
        List<ReservableReservationTimeDto> results = new ArrayList<>();
        available.forEach(time -> results.add(fromEntity(time, true)));
        notAvailable.forEach(time -> results.add(fromEntity(time, false)));
        return Collections.unmodifiableList(results);
    }
}
