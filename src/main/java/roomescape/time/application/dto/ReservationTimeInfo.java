package roomescape.time.application.dto;

import java.time.LocalTime;
import lombok.Builder;
import roomescape.time.domain.ReservationTime;

@Builder
public record ReservationTimeInfo(
        Long id,
        LocalTime startAt
) {
    public static ReservationTimeInfo from(ReservationTime time) {
        return ReservationTimeInfo.builder()
                .id(time.getId())
                .startAt(time.getStartAt())
                .build();
    }
}
