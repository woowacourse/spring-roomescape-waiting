package roomescape.time.application.dto;

import java.time.LocalTime;
import lombok.Builder;
import roomescape.time.domain.ReservationTime;

@Builder
public record ReservationTimeCommand(
        LocalTime startAt
) {
    public ReservationTime toEntity() {
        return ReservationTime.builder()
                .startAt(startAt)
                .build();
    }
}
